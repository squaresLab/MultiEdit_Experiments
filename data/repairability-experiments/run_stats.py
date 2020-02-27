import json
import csv
from scipy import stats

DEPENDENCY_TUPLE_INDEX_CTRL=0
DEPENDENCY_TUPLE_INDEX_FLOW=1
DEPENDENCY_TUPLE_INDEX_ANTI=2
DEPENDENCY_TUPLE_INDEX_OUTPUT=3
DEPENDENCY_TUPLE_INDEX_DATA=4
DEPENDENCY_TUPLE_INDEX_ANY=5

bears_single_module_bugs = list(range(1,141+1)) + [143,184,185,188,189,194,198, \
    201,202,204,207,209,210,213,215,216,217,218,219,220,221,223,224,225,226,230, \
    231,232,234,235,238,239,243,244,245,246,247,249,250,251]

bears_falsely_multiedit_bugs=[49,53,76,133]

def get_multi_edit_bugs():
    global bears_single_module_bugs

    with open('patch_locs.json') as f:
        patch_locs_json = json.load(f)

    multi_edit_bugs_d4j, multi_edit_bugs_bears = list(), list()

    for entry in patch_locs_json:
        bugId = str(entry['bugId']).replace(' ', '') #Remove the space in D4J bugIds
        patch = entry['patch']
        lines_edited = sum(len(edits) for edits in patch.values())
        if lines_edited > 1:
            if bugId[:5] == 'Bears':
                bugnum = int(bugId[6:])
                if bugnum in bears_single_module_bugs and bugnum not in bears_falsely_multiedit_bugs:
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

def get_tool_to_repaired_bugs():
    repairs_d4j = get_repairs_d4j()
    repairs_bears = get_repairs_bears()
    tools = repairs_d4j.keys()

    tools_to_repaired_bugs = dict()
    for tool in tools:
        tools_to_repaired_bugs[tool] = repairs_d4j[tool] + repairs_bears[tool]

    return tools_to_repaired_bugs

def percent(n_part, n_whole):
    p = 100 * n_part/n_whole
    return "{}%".format(p)

def print_dependency_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies):
    n_total_bugs_d4j = 395
    n_single_edit_bugs_d4j = n_total_bugs_d4j - len(multi_edit_bugs_d4j)
    n_multi_edit_bugs_d4j = n_total_bugs_d4j - n_single_edit_bugs_d4j
    n_bugs_with_ctrl_deps_d4j = sum(1 for bug in multi_edit_bugs_d4j if bug in dependencies and dependencies[bug][0])
    n_bugs_with_data_deps_d4j = sum(1 for bug in multi_edit_bugs_d4j if bug in dependencies and dependencies[bug][4])
    n_bugs_with_any_deps_d4j = sum(1 for bug in multi_edit_bugs_d4j if bug in dependencies and dependencies[bug][5])
    n_bugs_with_no_deps_d4j = len(multi_edit_bugs_d4j) - n_bugs_with_any_deps_d4j
    p_total_bugs_d4j = percent(n_total_bugs_d4j, n_total_bugs_d4j)
    p_single_edit_bugs_d4j = percent(n_single_edit_bugs_d4j, n_total_bugs_d4j)
    p_multi_edit_bugs_d4j = percent(n_multi_edit_bugs_d4j, n_total_bugs_d4j)
    p_bugs_with_ctrl_deps_d4j = percent(n_bugs_with_ctrl_deps_d4j, n_total_bugs_d4j)
    p_bugs_with_data_deps_d4j = percent(n_bugs_with_data_deps_d4j, n_total_bugs_d4j)
    p_bugs_with_any_deps_d4j = percent(n_bugs_with_any_deps_d4j, n_total_bugs_d4j)
    p_bugs_with_no_deps_d4j = percent(n_bugs_with_no_deps_d4j, n_total_bugs_d4j)
    pm_bugs_with_ctrl_deps_d4j = percent(n_bugs_with_ctrl_deps_d4j, n_multi_edit_bugs_d4j)
    pm_bugs_with_data_deps_d4j = percent(n_bugs_with_data_deps_d4j, n_multi_edit_bugs_d4j)
    pm_bugs_with_any_deps_d4j = percent(n_bugs_with_any_deps_d4j, n_multi_edit_bugs_d4j)
    pm_bugs_with_no_deps_d4j = percent(n_bugs_with_no_deps_d4j, n_multi_edit_bugs_d4j)


    print("Defects4J:")
    print("Total analyzed bugs: {} ({})".format(n_total_bugs_d4j, p_total_bugs_d4j))
    print("Single edit bugs: {} ({})".format(n_single_edit_bugs_d4j, p_single_edit_bugs_d4j))
    print("Multi edit bugs: {} ({})".format(n_multi_edit_bugs_d4j, p_multi_edit_bugs_d4j))
    print("Multi edit bugs with control deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_ctrl_deps_d4j, p_bugs_with_ctrl_deps_d4j, pm_bugs_with_ctrl_deps_d4j))
    print("Multi edit bugs with data deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_data_deps_d4j, p_bugs_with_data_deps_d4j, pm_bugs_with_data_deps_d4j))
    print("Multi edit bugs with any deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_any_deps_d4j, p_bugs_with_any_deps_d4j, pm_bugs_with_any_deps_d4j))
    print("Multi edit bugs with no deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_no_deps_d4j, p_bugs_with_no_deps_d4j, pm_bugs_with_no_deps_d4j))
    print()

    n_total_bugs_bears = 181 #184 single modules - 3 cassandra-reaper bugs that used a multi-module design
    n_single_edit_bugs_bears = n_total_bugs_bears - len(multi_edit_bugs_bears)
    n_multi_edit_bugs_bears = n_total_bugs_bears - n_single_edit_bugs_bears
    n_bugs_with_ctrl_deps_bears = sum(1 for bug in multi_edit_bugs_bears if bug in dependencies and dependencies[bug][0])
    n_bugs_with_data_deps_bears = sum(1 for bug in multi_edit_bugs_bears if bug in dependencies and dependencies[bug][4])
    n_bugs_with_any_deps_bears = sum(1 for bug in multi_edit_bugs_bears if bug in dependencies and dependencies[bug][5])
    n_bugs_with_no_deps_bears = len(multi_edit_bugs_bears) - n_bugs_with_any_deps_bears
    p_total_bugs_bears = percent(n_total_bugs_bears, n_total_bugs_bears)
    p_single_edit_bugs_bears = percent(n_single_edit_bugs_bears, n_total_bugs_bears)
    p_multi_edit_bugs_bears = percent(n_multi_edit_bugs_bears, n_total_bugs_bears)
    p_bugs_with_ctrl_deps_bears = percent(n_bugs_with_ctrl_deps_bears, n_total_bugs_bears)
    p_bugs_with_data_deps_bears = percent(n_bugs_with_data_deps_bears, n_total_bugs_bears)
    p_bugs_with_any_deps_bears = percent(n_bugs_with_any_deps_bears, n_total_bugs_bears)
    p_bugs_with_no_deps_bears = percent(n_bugs_with_no_deps_bears, n_total_bugs_bears)
    pm_bugs_with_ctrl_deps_bears = percent(n_bugs_with_ctrl_deps_bears, n_multi_edit_bugs_bears)
    pm_bugs_with_data_deps_bears = percent(n_bugs_with_data_deps_bears, n_multi_edit_bugs_bears)
    pm_bugs_with_any_deps_bears = percent(n_bugs_with_any_deps_bears, n_multi_edit_bugs_bears)
    pm_bugs_with_no_deps_bears = percent(n_bugs_with_no_deps_bears, n_multi_edit_bugs_bears)

    print("Bears:")
    print("Total analyzed bugs: {} ({})".format(n_total_bugs_bears, p_total_bugs_bears))
    print("Single edit bugs: {} ({})".format(n_single_edit_bugs_bears, p_single_edit_bugs_bears))
    print("Multi edit bugs: {} ({})".format(n_multi_edit_bugs_bears, p_multi_edit_bugs_bears))
    print("Multi edit bugs with control deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_ctrl_deps_bears, p_bugs_with_ctrl_deps_bears, pm_bugs_with_ctrl_deps_bears))
    print("Multi edit bugs with data deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_data_deps_bears, p_bugs_with_data_deps_bears, pm_bugs_with_data_deps_bears))
    print("Multi edit bugs with any deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_any_deps_bears, p_bugs_with_any_deps_bears, pm_bugs_with_any_deps_bears))
    print("Multi edit bugs with no deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_no_deps_bears, p_bugs_with_no_deps_bears, pm_bugs_with_no_deps_bears))
    print()

    n_total_bugs_combined          = n_total_bugs_d4j          + n_total_bugs_bears
    n_single_edit_bugs_combined    = n_single_edit_bugs_d4j    + n_single_edit_bugs_bears
    n_multi_edit_bugs_combined     = n_multi_edit_bugs_d4j     + n_multi_edit_bugs_bears
    n_bugs_with_ctrl_deps_combined = n_bugs_with_ctrl_deps_d4j + n_bugs_with_ctrl_deps_bears
    n_bugs_with_data_deps_combined = n_bugs_with_data_deps_d4j + n_bugs_with_data_deps_bears
    n_bugs_with_any_deps_combined  = n_bugs_with_any_deps_d4j  + n_bugs_with_any_deps_bears
    n_bugs_with_no_deps_combined   = n_bugs_with_no_deps_d4j   + n_bugs_with_no_deps_bears
    p_total_bugs_combined = percent(n_total_bugs_combined, n_total_bugs_combined)
    p_single_edit_bugs_combined = percent(n_single_edit_bugs_combined, n_total_bugs_combined)
    p_multi_edit_bugs_combined = percent(n_multi_edit_bugs_combined, n_total_bugs_combined)
    p_bugs_with_ctrl_deps_combined = percent(n_bugs_with_ctrl_deps_combined, n_total_bugs_combined)
    p_bugs_with_data_deps_combined = percent(n_bugs_with_data_deps_combined, n_total_bugs_combined)
    p_bugs_with_any_deps_combined = percent(n_bugs_with_any_deps_combined, n_total_bugs_combined)
    p_bugs_with_no_deps_combined = percent(n_bugs_with_no_deps_combined, n_total_bugs_combined)
    pm_bugs_with_ctrl_deps_combined = percent(n_bugs_with_ctrl_deps_combined, n_multi_edit_bugs_combined)
    pm_bugs_with_data_deps_combined = percent(n_bugs_with_data_deps_combined, n_multi_edit_bugs_combined)
    pm_bugs_with_any_deps_combined = percent(n_bugs_with_any_deps_combined, n_multi_edit_bugs_combined)
    pm_bugs_with_no_deps_combined = percent(n_bugs_with_no_deps_combined, n_multi_edit_bugs_combined)

    print("Combined:")
    print("Total analyzed bugs: {} ({})".format(n_total_bugs_combined, p_total_bugs_combined))
    print("Single edit bugs: {} ({})".format(n_single_edit_bugs_combined, p_single_edit_bugs_combined))
    print("Multi edit bugs: {} ({})".format(n_multi_edit_bugs_combined, p_multi_edit_bugs_combined))
    print("Multi edit bugs with control deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_ctrl_deps_combined, p_bugs_with_ctrl_deps_combined, pm_bugs_with_ctrl_deps_combined))
    print("Multi edit bugs with data deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_data_deps_combined, p_bugs_with_data_deps_combined, pm_bugs_with_data_deps_combined))
    print("Multi edit bugs with any deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_any_deps_combined, p_bugs_with_any_deps_combined, pm_bugs_with_any_deps_combined))
    print("Multi edit bugs with no deps: {} ({}, {} of multi edit bugs)" \
        .format(n_bugs_with_no_deps_combined, p_bugs_with_no_deps_combined, pm_bugs_with_no_deps_combined))
    print()

def partition_bugs_by_repairability(bugs_to_partition, tool_to_repaired_bugs):
    all_repairable_bugs = set()
    for tool, bugs_repaired_by_tool in tool_to_repaired_bugs.items():
        all_repairable_bugs |= set(bugs_repaired_by_tool)

    bugs_to_partition_set = set(bugs_to_partition)
    repairable_paritition = bugs_to_partition_set & all_repairable_bugs
    unrepairable_partition = bugs_to_partition_set - all_repairable_bugs
    return repairable_paritition, unrepairable_partition

def partition_bugs_by_dependency(bugs_to_partition, dependencies, dependency_tuple_index):
    all_dependent_bugs = set(bug for bug in dependencies if dependencies[bug][dependency_tuple_index])

    bugs_to_partition_set = set(bugs_to_partition)
    dependent_partition = bugs_to_partition_set & all_dependent_bugs
    nondependent_partition = bugs_to_partition_set - all_dependent_bugs
    return dependent_partition, nondependent_partition

def print_repairability_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, tool_to_repaired_bugs):
    repairable_d4j, nonrepairable_d4j = partition_bugs_by_repairability(multi_edit_bugs_d4j, tool_to_repaired_bugs)
    num_repairable_d4j, num_nonrepairable_d4j = len(repairable_d4j), len(nonrepairable_d4j)
    print("D4J:")
    print("Bugs repaired by any technique: {}".format(num_repairable_d4j))
    print("Bugs not repaired by any technique: {}".format(num_nonrepairable_d4j))
    print()

    repairable_bears, nonrepairable_bears = partition_bugs_by_repairability(multi_edit_bugs_bears, tool_to_repaired_bugs)
    num_repairable_bears, num_nonrepairable_bears = len(repairable_bears), len(nonrepairable_bears)
    print("Bears:")
    print("Bugs repaired by any technique: {}".format(num_repairable_bears))
    print("Bugs not repaired by any technique: {}".format(num_nonrepairable_bears))
    print()

def run_chi2(dependent_bugs, nondependent_bugs, repairable_bugs, nonrepairable_bugs, message):
    contingency_table = [[len(dependent_bugs & repairable_bugs), len(nondependent_bugs & repairable_bugs)],
                         [len(dependent_bugs & nonrepairable_bugs), len(nondependent_bugs & nonrepairable_bugs)]]

    chi2, pval, df = stats.chi2_contingency(contingency_table)[0:3]
    print(message)
    print('\tdependent\tnondependent')
    print('repairable\t{}\t{}'.format(contingency_table[0][0], contingency_table[0][1]))
    print('nonrepairable\t{}\t{}'.format(contingency_table[1][0], contingency_table[1][1]))
    print('p-value:', pval)
    print()

def run_fisher_exact(dependent_bugs, nondependent_bugs, repairable_bugs, nonrepairable_bugs, message):
    contingency_table = [[len(dependent_bugs & repairable_bugs), len(nondependent_bugs & repairable_bugs)],
                         [len(dependent_bugs & nonrepairable_bugs), len(nondependent_bugs & nonrepairable_bugs)]]

    oddsratio, pval = stats.fisher_exact(contingency_table)
    print(message)
    print('\tdependent\tnondependent')
    print('repairable\t{}\t{}'.format(contingency_table[0][0], contingency_table[0][1]))
    print('nonrepairable\t{}\t{}'.format(contingency_table[1][0], contingency_table[1][1]))
    print('p-value:', pval)
    print()

def test_dependency_and_repairability(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies, tool_to_repaired_bugs):
    ctrl_dependent_d4j, ctrl_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_d4j, data_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_d4j, any_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_d4j, nonrepairable_d4j = partition_bugs_by_repairability(multi_edit_bugs_d4j, tool_to_repaired_bugs)

    run_chi2(ctrl_dependent_d4j, ctrl_nondependent_d4j, repairable_d4j, nonrepairable_d4j, \
    "D4J: Chi-squared between control dependency and repairability")
    run_chi2(data_dependent_d4j, data_nondependent_d4j, repairable_d4j, nonrepairable_d4j, \
    "D4J: Chi-squared between data dependency and repairability")
    run_chi2(any_dependent_d4j, any_nondependent_d4j, repairable_d4j, nonrepairable_d4j, \
    "D4J: Chi-squared between control|data dependency and repairability")

    ctrl_dependent_bears, ctrl_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_bears, data_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_bears, any_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_bears, nonrepairable_bears = partition_bugs_by_repairability(multi_edit_bugs_bears, tool_to_repaired_bugs)

    run_fisher_exact(ctrl_dependent_bears, ctrl_nondependent_bears, repairable_bears, nonrepairable_bears, \
    "Bears: Fisher's Exact Test between control dependency and repairability")
    run_fisher_exact(data_dependent_bears, data_nondependent_bears, repairable_bears, nonrepairable_bears, \
    "Bears: Fisher's Exact Test between data dependency and repairability")
    run_fisher_exact(any_dependent_bears, any_nondependent_bears, repairable_bears, nonrepairable_bears, \
    "Bears: Fisher's Exact Test between control|data dependency and repairability")

if __name__=='__main__':
    multi_edit_bugs_d4j, multi_edit_bugs_bears = get_multi_edit_bugs() #collection of multi-edit bugs
    dependencies = get_dependencies() #maps bugId -> dependency 6-tuple
    tool_to_repaired_bugs = get_tool_to_repaired_bugs()
    print_dependency_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies)
    print_repairability_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, tool_to_repaired_bugs)
    test_dependency_and_repairability(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies, tool_to_repaired_bugs)
