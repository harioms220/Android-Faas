package com.rizzle.sdk.faas.demo.utils

import com.rizzle.sdk.faas.demo.models.ItemsInfo

object DataGenerator {
    @JvmStatic
    fun getItemsData(): List<ItemsInfo> {
        val itemList = ArrayList<ItemsInfo>()
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1621072156002-e2fccdc0b176?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8c2hpcnR8ZW58MHx8MHx8&auto=format&fit=crop&w=500&q=60",
                "Louis Philippe", "₹2100", "Pure Lenin"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1581655353564-df123a1eb820?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8M3x8c2hpcnRzfGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                "US POLO ASSN.", "₹1900", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1620012253295-c15cc3e65df4?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTJ8fHNoaXJ0c3xlbnwwfHwwfHw%3D&auto=format&fit=crop&w=500&q=60",
                "H&M", "₹1780", "Lycra"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1594938291221-94f18cbb5660?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTh8fHNoaXJ0c3xlbnwwfHwwfHw%3D&auto=format&fit=crop&w=500&q=60",
                "ZARA", "₹4000", "Mixed"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://media.istockphoto.com/id/1333923906/photo/cheerful-asian-young-man-in-black-clothes.jpg?b=1&s=170667a&w=0&k=20&c=cu7QJc3U-yjZk6ke2cncoJq2xMAtjpqlFOr4pQ9FaOw=",
                "Louis Vuitton", "₹7400", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8OXx8c2hpcnR8ZW58MHx8MHx8&auto=format&fit=crop&w=500&q=60",
                "PRADA", "₹6000", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NHx8c2hpcnRzfGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                "Calvin Klein", "₹16000", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSU54UXiuPOCh4NymyhtzvKl0bKnrD33nZ3LQ&usqp=CAU",
                "GUCCI", "₹9000", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://media.istockphoto.com/id/1334098846/photo/close-up-orange-t-shirt-cotton-man-pattern-isolated-on-white.jpg?b=1&s=170667a&w=0&k=20&c=WP0zJdEfLwnsUD8hROulV_92j6x01aJHedFSvaX8vV0=",
                "Ralph Lauren Corp.", "₹3000", "Mixed Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8OHx8Y2xvdGhlc3xlbnwwfHwwfHw%3D&auto=format&fit=crop&w=500&q=60",
                "Tommy Hilfiger", "₹2000", "Mixed Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1581655353564-df123a1eb820?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8M3x8c2hpcnRzfGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                "UCB", "₹2000", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1620012253295-c15cc3e65df4?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTJ8fHNoaXJ0c3xlbnwwfHwwfHw%3D&auto=format&fit=crop&w=500&q=60",
                "Van Heusen", "₹3000", "Mixed Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1594938291221-94f18cbb5660?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTh8fHNoaXJ0c3xlbnwwfHwwfHw%3D&auto=format&fit=crop&w=500&q=60",
                "Peter England", "₹5000", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1621072156002-e2fccdc0b176?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8c2hpcnR8ZW58MHx8MHx8&auto=format&fit=crop&w=500&q=60",
                "Louis Philippe", "₹2100", "Pure Lenin"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8OXx8c2hpcnR8ZW58MHx8MHx8&auto=format&fit=crop&w=500&q=60",
                "US POLO ASSN.", "₹1900", "Pure Cotton"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTJ8fHNoaXJ0fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                "H&M", "₹1780", "Lycra"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MjB8fHNoaXJ0fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                "ZARA", "₹4000", "Mixed"
            )
        )
        itemList.add(
            ItemsInfo(
                "https://images.unsplash.com/photo-1548864789-7393f2b4baa5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MzZ8fHNoaXJ0fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                "Louis Vuitton", "₹7400", "Pure Cotton"
            )
        )

        return itemList
    }
}