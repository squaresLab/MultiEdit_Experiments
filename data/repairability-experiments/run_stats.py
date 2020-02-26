import json
import csv

def get_multi_edit_bugs():
    with open('patch_locs.json') as f:
        patch_locs_json = json.load(f)

    multi_edit_bugs = list()

    for entry in patch_locs_json:
        bugId = entry['bugId'].replace(' ', '') #Remove the space in D4J bugIds
        patch = entry['patch']
        lines_edited = sum(len(edits) for edits in patch.values())
        if lines_edited > 1:
            multi_edit_bugs.append(bugId)

    return multi_edit_bugs

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
    multi_edit_bugs = get_multi_edit_bugs() #collection of multi-edit bugs
    dependencies = get_dependencies() #maps bugId -> 6-Tuple info on dependencies

#todo: get info on which bugs are repairable by which techniques
