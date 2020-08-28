#!/usr/bin/env bash

output_dir="../../data/dependency-experiments/d4j/"
#output_dir=~/scheisse
function analyze-d4j-bug-range {
    project=$1
    startBugID=$2
    endBugID=$3

    for ((id = $startBugID ; id <= $endBugID ; id++)); do
        ./find-patch-dependencies-d4j.sh -p $project -b $id -d $output_dir
    done
}

#analyze-d4j-bug-range Cli 1 5
#analyze-d4j-bug-range Cli 7 40 #TODO RUN
#analyze-d4j-bug-range Codec 1 18
#analyze-d4j-bug-range Collections 25 28
#analyze-d4j-bug-range Compress 1 47
#analyze-d4j-bug-range Csv 1 16
#analyze-d4j-bug-range Gson 1 18
#analyze-d4j-bug-range JacksonCore 1 26
#analyze-d4j-bug-range JacksonDatabind 1 112
#analyze-d4j-bug-range JacksonXml 1 6
#analyze-d4j-bug-range Jsoup 1 93
#analyze-d4j-bug-range JxPath 1 22

analyze-d4j-bug-range JacksonXml 1 6
