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
            dependency_info = [True if v == 'True' else False for v in row[1:]]
            dependencies[bugId] = dependency_info

    return dependencies

def get_repairs_bears():
    repairs = dict() #maps tool -> bugIds of bugs repaired by the tool

    with open('repair-them-all/Bears.csv') as f:
        reader=csv.reader(f)
        for row in reader:
            tool = row[0]
            bugnums = row[1:] #may be empty
            bugIds = ['Bears-{}'.format(bugnum) for bugnum in bugnums]
            repairs[tool] = bugIds

    return repairs

def get_repairs_d4j():
    repairs = dict() #maps tool -> bugIds of bugs repaired by the tool

    with open('repair-them-all/defects4j.csv') as f:
        reader=csv.reader(f)
        for row in reader:
            tool = row[0]
            proj = row[1]
            bugnums = row[2:] #may be empty
            bugIds = ['{}{}'.format(proj, bugnum) for bugnum in bugnums]

            if tool not in repairs:
                repairs[tool] = list()

            repairs[tool] += bugIds

    return repairs

def percent(n_part, n_whole):
    p = 100 * n_part/n_whole
    return "{}%".format(p)

def print_dependency_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies):
    n_total_bugs_d4j = 395
    n_single_edit_bugs_d4j = n_total_bugs_d4j - len(multi_edit_bugs_d4j)
    n_bugs_with_ctrl_deps_d4j = sum(1 for bug in multi_edit_bugs_d4j if bug in dependencies and dependencies[bug][0])
    n_bugs_with_data_deps_d4j = sum(1 for bug in multi_edit_bugs_d4j if bug in dependencies and dependencies[bug][4])
    n_bugs_with_any_deps_d4j = sum(1 for bug in multi_edit_bugs_d4j if bug in dependencies and dependencies[bug][5])
    n_bugs_with_no_deps_d4j = len(multi_edit_bugs_d4j) - n_bugs_with_any_deps_d4j

    print("Defects4J:")
    print("Total analyzed bugs: {} ({})".format(n_total_bugs_d4j, percent(n_total_bugs_d4j, n_total_bugs_d4j)))
    print("Single edit bugs: {} ({})".format(n_single_edit_bugs_d4j, percent(n_single_edit_bugs_d4j, n_total_bugs_d4j)))
    print("Multi edit bugs with control deps: {} ({})".format(n_bugs_with_ctrl_deps_d4j, percent(n_bugs_with_ctrl_deps_d4j, n_total_bugs_d4j)))
    print("Multi edit bugs with data deps: {} ({})".format(n_bugs_with_data_deps_d4j, percent(n_bugs_with_data_deps_d4j, n_total_bugs_d4j)))
    print("Multi edit bugs with any deps: {} ({})".format(n_bugs_with_any_deps_d4j, percent(n_bugs_with_any_deps_d4j, n_total_bugs_d4j)))
    print("Multi edit bugs with no deps: {} ({})".format(n_bugs_with_no_deps_d4j, percent(n_bugs_with_no_deps_d4j, n_total_bugs_d4j)))

    n_total_bugs_bears = 181 #184 single modules - 3 cassandra-reaper bugs that used a multi-module design
    n_single_edit_bugs_bears = n_total_bugs_bears - len(multi_edit_bugs_bears)
    n_bugs_with_ctrl_deps_bears = sum(1 for bug in multi_edit_bugs_bears if bug in dependencies and dependencies[bug][0])
    n_bugs_with_data_deps_bears = sum(1 for bug in multi_edit_bugs_bears if bug in dependencies and dependencies[bug][4])
    n_bugs_with_any_deps_bears = sum(1 for bug in multi_edit_bugs_bears if bug in dependencies and dependencies[bug][5])
    n_bugs_with_no_deps_bears = len(multi_edit_bugs_bears) - n_bugs_with_any_deps_bears

    print("Bears:")
    print("Total analyzed bugs: {} ({})".format(n_total_bugs_bears, percent(n_total_bugs_bears, n_total_bugs_bears)))
    print("Single edit bugs: {} ({})".format(n_single_edit_bugs_bears, percent(n_single_edit_bugs_bears, n_total_bugs_bears)))
    print("Multi edit bugs with control deps: {} ({})".format(n_bugs_with_ctrl_deps_bears, percent(n_bugs_with_ctrl_deps_bears, n_total_bugs_bears)))
    print("Multi edit bugs with data deps: {} ({})".format(n_bugs_with_data_deps_bears, percent(n_bugs_with_data_deps_bears, n_total_bugs_bears)))
    print("Multi edit bugs with any deps: {} ({})".format(n_bugs_with_any_deps_bears, percent(n_bugs_with_any_deps_bears, n_total_bugs_bears)))
    print("Multi edit bugs with no deps: {} ({})".format(n_bugs_with_no_deps_bears, percent(n_bugs_with_no_deps_bears, n_total_bugs_bears)))

if __name__=='__main__':
    multi_edit_bugs_d4j, multi_edit_bugs_bears = get_multi_edit_bugs() #collection of multi-edit bugs
    dependencies = get_dependencies() #maps bugId -> ()
    repairs_bears, repairs_d4j = get_repairs_bears(), get_repairs_d4j()
    print_dependency_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies)
