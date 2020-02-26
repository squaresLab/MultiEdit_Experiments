import json
import csv

def get_multi_edit_bugs():
    with open('patch_locs.json') as f:
        patch_locs_json = json.load(f)

    multi_edit_bugs_d4j, multi_edit_bugs_bears = list(), list()

    for entry in patch_locs_json:
        bugId = str(entry['bugId']).replace(' ', '') #Remove the space in D4J bugIds
        patch = entry['patch']
        lines_edited = sum(len(edits) for edits in patch.values())
        if lines_edited > 1:
            if bugId[:5] == 'Bears':
                multi_edit_bugs_bears.append(bugId)
            else:
                multi_edit_bugs_d4j.append(bugId)

    return multi_edit_bugs_d4j, multi_edit_bugs_bears

def get_dependencies():
    dependencies = dict() #maps bugId -> 6-Tuple info on dependencies

    with open('dependencies.csv') as f:
        dependencies_reader=csv.reader(f)
        for row in dependencies_reader:
            bugId = row[0]
            dependency_info = row[1:]
            dependencies[bugId] = dependency_info

    return dependencies

if __name__=='__main__':
    multi_edit_bugs_d4j, multi_edit_bugs_bears = get_multi_edit_bugs() #collection of multi-edit bugs
    dependencies = get_dependencies() #maps bugId -> 6-Tuple info on dependencies
    print(multi_edit_bugs_bears)

#todo: get info on which bugs are repairable by which techniques
