package com.rizzle.sdk.faas.uistylers

import android.graphics.Color
import androidx.annotation.RestrictTo
import androidx.lifecycle.MutableLiveData
import com.rizzle.sdk.faas.helpers.*
import com.rizzle.sdk.faas.preferences.LATEST_CONFIG_VERSION
import com.rizzle.sdk.faas.uistylers.models.*
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.faas.utils.InternalUtils.filesDir
import com.rizzle.sdk.faas.utils.InternalUtils.pref
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

/**
 * UI Configuration class helpful for handling the remote ui configurable
 * text styles, colors and icons.
 *
 * Each text style in the system is tagged with some key.
 * Similarly, some colors and icons are also tagged.
 * Some properties of these tagged ui elements are remotely configurable.
 * For example. fonts and size of some tagged text component can be changed remotely.
 * Similarly for icons and colors also.
 *
 * The json configuration file gives us the keys and values for the
 * ui elements which are tagged in the system.
 *
 * The overall flow goes like this. (explained in steps)
 * 1. First we get the configuration data from the server at application launch time.
 * 2. Then, if any previous uiconfig.json file is stored, we will use it as the configuration until we compare it with the version of new
 *    configuration data we received from server.
 * 3. If new version is available, we download the new assets(icons and fonts) corresponding to that
 *    new configuration in the background in some temporary folder.
 * 4. Once all assets are downloaded, we rename the temporary folder as main folder, otherwise
 *    discard all the temporary folder in case of error.
 * 5. All new screens opening up, that are using the tagged ui components will be able to use the new
 *    configuration.
 *
 * There are factories available in this class corresponding to each type of ui component.
 * Factories will provide the corresponding properties mainly used to map the xml custom attribute to
 * actual configuration behind that attribute.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object UiConfig {

    private val tag = javaClass.name
    private const val MAX_TIMES_TO_RETRY = 2L

    /** base folder path containing all the assets */
    private val assetsPath = "${filesDir()}/rizzle-assets"

    /** path of folder containing fonts */
    val fontsPath = "$assetsPath/fonts"

    /** path of folder containing icons */
    val iconsPath = "$assetsPath/icons"

    /** path of folder containing configuration data */
    private val configFilePath = "$assetsPath/uiconfig.json"
    private val subscriptions = CompositeDisposable()

    /** temporary folder path for temporary storing new assets */
    private val tempFolderPath = "${filesDir()}/temp-assets"
    private val tempFontPath = "$tempFolderPath/fonts"
    private val tempIconsPath = "$tempFolderPath/icons"
    private val tempConfigFilePath = "$tempFolderPath/uiconfig.json"

    /**
     * This variable will hold all the information
     * regarding the textStyles, colors and icons.
     */
    private var config: Config? = null

    /** flag used for checking if any stored configuration is parsed if available*/
    var isParsed = MutableLiveData<Boolean>()

    // region parsing config and downloading assets

    /**
     * This will parse the old configuration file if stored
     * and fetch the new configuration, if any version is changed,
     * it will download all the new assets and after successful download,
     * new configuration will be used.
     */
    fun fetchRemoteUIConfig() {
        // until we are fetching the configuration remotely for any changes, old configuration is used.
        parseConfigFromFile(configFilePath)

        // fetching the api for any configuration changes on remote
        subscriptions += InternalUtils.networkApis.getRemoteUIconfig()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ parseConfigAndCheckForChanges(it) }, {
                RizzleLogging.logError(Exception("Error while fetching the remote ui config, using the old config file", it))
            })
    }

    /**
     * Use this method to parse the configuration file
     * containing [TextStyle]s, [ColorConfig]s and [IconConfig]s
     */
    private fun parseConfigFromFile(filePath: String = "") {
        try {
            readJsonFromFileAsync(filePath)
                .doOnSubscribe { Timber.tag(tag).d("reading existing configuration and checking for any configuration changes on server...") }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    config = InternalUtils.jsonSerializer.convertJsonToPojo(it, AppConfig::class.java)?.config
                    Timber.tag(tag).d("existing configuration: $config")
                    isParsed.value = true
                }, {
                    RizzleLogging.logError(it)
                    isParsed.value = true
                })
        } catch (ex: FileNotFoundException) {
            // if uiconfig.json file not found, then if assets are present,
            // they are invalid and just delete them
            RizzleLogging.logError(ex)
            assetsPath.deleteFile()
            isParsed.value = true
        }catch (ex: Exception){
            RizzleLogging.logError(ex)
            isParsed.value = true
        }
    }

    private fun parseConfigAndCheckForChanges(jsonString: String) {
        val newConfig = InternalUtils.jsonSerializer.convertJsonToPojo(jsonString, AppConfig::class.java)?.config
        newConfig?.version?.let { newVersion ->
            val lastConfigVersion = pref.getInt(LATEST_CONFIG_VERSION, -1)
            if (lastConfigVersion < newVersion) {
                /** new config file changed remotely, hence storing the
                 *  new configuration assets in temp folder,
                 *  once new one are downloaded successfully, deleting the old ones.
                 */
                Timber.tag(tag).d("new configuration found on remote, new version: $newVersion")
                subscriptions += downloadAssets(newConfig)
                    .andThen(File(tempConfigFilePath).writeStringAsync(jsonString))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Timber.tag(tag).d("Assets downloaded successfully")
                            // deleting the old assets
                            assetsPath.deleteFile()
                            // saving new downloaded assets as main assets
                            File(tempFolderPath).renameTo(File(assetsPath))
                            newConfig.version.let {
                                pref.put(LATEST_CONFIG_VERSION to it)
                            }
                            config = newConfig
                            // invalidating all the factories to recreate as per new config.
                            invalidateAllFactories()
                        }) {
                        RizzleLogging.logError(Exception("Error while downloading all the assets", it))
                        // deleting all the assets if downloading failed for any single icon or textStyle
                        tempFolderPath.deleteFile()
                    }
            }else{
                Timber.tag(tag).d("no new configuration found on server.")
            }
        }
    }

    private fun invalidateAllFactories() {
        TextStyleFactory.invalidate()
        IconFactory.invalidate()
        ColorFactory.invalidate()
    }

    private fun downloadAssets(config: Config): Completable {
        if (tempFolderPath.fileExists()) tempFolderPath.deleteFile()
        tempFolderPath.mkdirs()
        return downloadFonts(config).mergeWith(downloadIcons(config))

    }

    private fun downloadFonts(config: Config): Completable {
        tempFontPath.mkdirs()
        return Observable.fromIterable(config.fonts)
            .flatMapCompletable { downloadFont(it).subscribeOn(Schedulers.io()) }
            .doOnComplete { Timber.tag(tag).d("Fonts downloaded successfully") }
    }

    private fun downloadFont(fontConfig: FontConfig): Completable {
        with(fontConfig) {
            return InternalUtils.networkApis.downloadFile(fontUrl, File("$tempFontPath$relativeFilePath"))
                .retry(MAX_TIMES_TO_RETRY)
                .doOnComplete { Timber.tag(tag).d("Font downloaded successfully: $fontUrl") }
        }
    }

    private fun downloadIcons(config: Config): Completable {
        tempIconsPath.mkdirs()
        return Observable.fromIterable(config.icons)
            .flatMapCompletable { downloadIcon(it).subscribeOn(Schedulers.io()) }
            .doOnComplete { Timber.tag(tag).d("Icons downloaded successfully") }
    }

    private fun downloadIcon(iconConfig: IconConfig): Completable {
        with(iconConfig) {
            return InternalUtils.networkApis.downloadFile(iconUrl, File("$tempIconsPath/$filePath"))
                .retry(MAX_TIMES_TO_RETRY)
                .doOnComplete { Timber.tag(tag).d("Icon downloaded successfully: $iconUrl") }
        }
    }

    // endregion

    // region factories

    object TextStyleFactory {

        private val mapOfTextFiles = hashMapOf<String, TextStyle>()

        init {
            createMapFromConfig()
            Timber.tag(tag).d("Available TextStyles: $mapOfTextFiles")
        }

        private fun createMapFromConfig() {
            val mapOfFonts = hashMapOf<String, FontConfig>()
            config?.fonts?.forEach { fontConfig ->
                mapOfFonts[fontConfig.name.lowercase()] = fontConfig
            }
            config?.textStyles?.forEach { textStyle ->
                textStyle.fontConfig = mapOfFonts[textStyle.font]
                mapOfTextFiles[textStyle.name.lowercase()] = textStyle
            }
        }

        fun getTextStyle(styleType: StyleType): TextStyle? {
            return mapOfTextFiles[styleType.name.lowercase()]
        }

        fun invalidate() {
            mapOfTextFiles.clear()
            createMapFromConfig()
        }
    }

    object ColorFactory {

        private val mapOfColors = hashMapOf<String, ColorConfig>()

        init {
            createMapFromConfig()
            Timber.tag(tag).d("Available Colors: $mapOfColors")
        }

        private fun createMapFromConfig() {
            config?.colors?.forEach { color ->
                mapOfColors[color.name.lowercase()] = color
            }
        }

        fun getColor(colorType: ColorType): Int? {
            return mapOfColors[colorType.name.lowercase()]?.hexCode?.let { Color.parseColor(it) }
        }

        fun invalidate() {
            mapOfColors.clear()
            createMapFromConfig()
        }
    }

    object IconFactory {

        private val mapOfIcons = hashMapOf<String, IconConfig>()

        init {
            createMapFromConfig()
            Timber.tag(tag).d("Available Icons: $mapOfIcons")
        }

        private fun createMapFromConfig() {
            config?.icons?.forEach { iconConfig ->
                mapOfIcons[iconConfig.name.lowercase()] = iconConfig
            }
        }

        fun getIcon(iconType: IconType): IconConfig? {
            return mapOfIcons[iconType.name.lowercase()]
        }

        fun invalidate() {
            mapOfIcons.clear()
            createMapFromConfig()
        }
    }

    //endregion

    val cardCornerRadius
        get() = config?.cardCornerRadius ?: -1f
    val capsuleCornerRadius
        get() = config?.pillCornerRadius ?: -1f

    fun cancelDownloadingAssets() {
        subscriptions.clear()
        tempFolderPath.deleteFile()
    }
}

// region styles, icons and colors definitions

@RestrictTo(RestrictTo.Scope.LIBRARY)
enum class StyleType {
    HEADING_1,
    HEADING_2_EXTRA_BOLD,
    HEADING_2_SEMI_BOLD,
    TITLE_1,
    TITLE_2_REGULAR,
    TITLE_2_SEMI_BOLD,
    SUBTITLE_REGULAR,
    SUBTITLE_SEMI_BOLD,
    SUBTITLE_BOLD,
    BODY_1_REGULAR,
    BODY_1_SEMI_BOLD,
    BODY_2_SEMI_BOLD,
    BODY_2_REGULAR,
    CAPTION
}

@RestrictTo(RestrictTo.Scope.LIBRARY)
enum class ColorType {
    GRAY_50,
    GRAY_100,
    GRAY_200,
    GRAY_300,
    GRAY_400,
    GRAY_500,
    GRAY_600,
    GRAY_700,
    GRAY_800,
    GRAY_900,
    PRIMARY,
    SECONDARY_1,
    SECONDARY_2,
    SHADE_1,
    SHADE_2
}

@RestrictTo(RestrictTo.Scope.LIBRARY)
enum class IconType {
    IC_BACK,
    IC_SHARE,
    IC_SHARE_LINK,
    IC_COPY_LINK,
    IC_MORE,

    // This two icon types are only required for [LikeUnlikeImageView]
    IC_LIKE,
    IC_UNLIKE
}

//endregion