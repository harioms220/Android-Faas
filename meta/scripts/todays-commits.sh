#lists all the commits done today on repo

git fetch --quiet

git --no-pager log --all --date=format:'%a %H:%M'  --since=1.days.ago --pretty=format:'%H,%an,%ad,%s' | awk -F ',' '{
  author = $2;
  commit_hash = $1;
  commit_date = $3;
  commit_line = $4;

  authors[author] = 1;
  branch_counts[author] = 0;

  branch_cmd = "git --no-pager branch -a --contains " $1 " | xargs";
  while (branch_cmd | getline branch) {
    if (author_branches[author, branch] == 0) {
      author_branches[author, branch_counts[author]] = branch;
      branch_counts[author]++;
    }

    commit_counts[author, branch]++;
    branch_commits[author, branch, commit_counts[author, branch] - 1] = commit_hash;
  }

  commit_dates[commit_hash] = commit_date;
  commit_lines[commit_hash] = commit_line;
}

END {
  for (author in authors) {
    print "☗  " author;
    for (i = 0; i < branch_counts[author]; i++) {
      branch = author_branches[author, i];
      print "   ∞  " branch;
      for (j = 0; j < commit_counts[author, branch]; j++) {
        commit_hash = branch_commits[author, branch, j];
        print "      ✓ " commit_dates[commit_hash] " · " commit_lines[commit_hash] " · "commit_hash"";
      }
    }
  }
}'
