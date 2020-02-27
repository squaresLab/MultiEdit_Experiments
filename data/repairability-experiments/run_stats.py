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

def get_bugId_from_Serenas_data_line_d4j(coverage_line):
    coverage_line_stripped = coverage_line.strip()
    proj_raw, num_raw = coverage_line_stripped.split(':')
    proj = proj_raw[0] + proj_raw[1:].lower()
    num = str(int(num_raw))
    bugId = proj + num
    return bugId

def get_bugId_from_Serenas_data_line_bears(coverage_line):
    coverage_line_stripped = coverage_line.strip()
    branch, bugnum_raw = coverage_line.split(':')
    bugnum = int(bugnum_raw)
    bugId = 'Bears-{}'.format(bugnum)
    return bugId

def get_multi_chunk_bugs():
    multi_chunk_bugs_d4j = list()
    with open('multi-chunk-bugs/multi_chunk_d4j.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_d4j(line)
            multi_chunk_bugs_d4j.append(bugId)

    multi_chunk_bugs_bears = list()
    with open('multi-chunk-bugs/multi_chunk_bears.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_bears(line)
            multi_chunk_bugs_bears.append(bugId)

    return multi_chunk_bugs_d4j, multi_chunk_bugs_bears

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

def get_coverage_d4j():
    disjoint = set()
    with open('coverage/d4j/disjoint.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_d4j(line)
            disjoint.add(bugId)

    inBetween = set()
    with open('coverage/d4j/inBetween.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_d4j(line)
            inBetween.add(bugId)

    same = set()
    with open('coverage/d4j/same.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_d4j(line)
            same.add(bugId)

    return disjoint, inBetween, same

def get_coverage_bears():
    disjoint = set()
    with open('coverage/d4j/disjoint.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_bears(line)
            disjoint.add(bugId)

    inBetween = set()
    with open('coverage/d4j/inBetween.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_bears(line)
            inBetween.add(bugId)

    same = set()
    with open('coverage/d4j/same.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_bears(line)
            same.add(bugId)

    return disjoint, inBetween, same

def get_symptoms(grouping_method):
    symptoms_to_bugs = dict() #maps symptom to set of bugIds

    with open('symptoms/{}/symptoms.csv'.format(grouping_method)) as f:
        reader=csv.reader(f)
        for row in reader:
            bugId_raw = row[0] #different format than before, don't use helper functions
            symptoms = row[1:]
            project_raw, bugnum_raw = bugId_raw.split(':')
            bugnum = int(bugnum_raw)
            if project_raw == 'BEARS':
                bugId = 'Bears-{}'.format(bugnum)
            else:
                bugId = project_raw[0] + project_raw[1:].lower() + str(bugnum)

            for symptom in symptoms:
                if symptom not in symptoms_to_bugs:
                    symptoms_to_bugs[symptom] = set()
                symptoms_to_bugs[symptom].add(bugId)

    return symptoms_to_bugs

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

def run_chi2(r0set, r1set, c0set, c1set, message, r0, r1, c0, c1):
    contingency_table = [[len(r0set & c0set), len(r0set & c1set)],
                         [len(r1set & c0set), len(r1set & c1set)]]

    chi2, pval, df = stats.chi2_contingency(contingency_table)[0:3]
    print(message)
    print('\t{}\t{}'.format(c0, c1))
    print('{}\t{}\t{}'.format(r0, contingency_table[0][0], contingency_table[0][1]))
    print('{}\t{}\t{}'.format(r1, contingency_table[1][0], contingency_table[1][1]))
    print('p-value:', pval)
    print()

def run_fisher_exact(r0set, r1set, c0set, c1set, message, r0, r1, c0, c1):
    contingency_table = [[len(r0set & c0set), len(r0set & c1set)],
                         [len(r1set & c0set), len(r1set & c1set)]]

    oddsratio, pval = stats.fisher_exact(contingency_table)
    print(message)
    print('\t{}\t{}'.format(c0, c1))
    print('{}\t{}\t{}'.format(r0, contingency_table[0][0], contingency_table[0][1]))
    print('{}\t{}\t{}'.format(r1, contingency_table[1][0], contingency_table[1][1]))
    print('p-value:', pval)
    print()

def test_dependency_and_repairability(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies, tool_to_repaired_bugs):
    ctrl_dependent_d4j, ctrl_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_d4j, data_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_d4j, any_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_d4j, nonrepairable_d4j = partition_bugs_by_repairability(multi_edit_bugs_d4j, tool_to_repaired_bugs)

    run_chi2(repairable_d4j, nonrepairable_d4j, ctrl_dependent_d4j, ctrl_nondependent_d4j, \
    "D4J: Chi-squared between control dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_chi2(repairable_d4j, nonrepairable_d4j, data_dependent_d4j, data_nondependent_d4j, \
    "D4J: Chi-squared between data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_chi2(repairable_d4j, nonrepairable_d4j, any_dependent_d4j, any_nondependent_d4j, \
    "D4J: Chi-squared between control|data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

    ctrl_dependent_bears, ctrl_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_bears, data_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_bears, any_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_bears, nonrepairable_bears = partition_bugs_by_repairability(multi_edit_bugs_bears, tool_to_repaired_bugs)

    run_fisher_exact(repairable_bears, nonrepairable_bears, ctrl_dependent_bears, ctrl_nondependent_bears, \
    "Bears: Fisher's Exact Test between control dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_fisher_exact(repairable_bears, nonrepairable_bears, data_dependent_bears, data_nondependent_bears, \
    "Bears: Fisher's Exact Test between data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_fisher_exact(repairable_bears, nonrepairable_bears, any_dependent_bears, any_nondependent_bears, \
    "Bears: Fisher's Exact Test between control|data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

    ctrl_dependent_all, ctrl_nondependent_all = ctrl_dependent_d4j | ctrl_dependent_bears, ctrl_nondependent_d4j | ctrl_nondependent_bears
    data_dependent_all, data_nondependent_all = data_dependent_d4j | data_dependent_bears, data_nondependent_d4j | data_nondependent_bears
    any_dependent_all, any_nondependent_all = any_dependent_d4j | any_dependent_bears, any_nondependent_d4j | any_nondependent_bears
    repairable_all, nonrepairable_all = repairable_d4j | repairable_bears, nonrepairable_d4j | nonrepairable_bears

    run_chi2(repairable_all, nonrepairable_all, ctrl_dependent_all, ctrl_nondependent_all, \
    "Combined D4J|Bears: Chi-squared between control dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_chi2(repairable_all, nonrepairable_all, data_dependent_all, data_nondependent_all, \
    "Combined D4J|Bears: Chi-squared between data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_chi2(repairable_all, nonrepairable_all, any_dependent_all, any_nondependent_all, \
    "Combined D4J|Bears: Chi-squared between control|data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

def test_coverage_and_repairability(multi_chunk_bugs_d4j, multi_chunk_bugs_bears, coverage_partitions_d4j, coverage_partitions_bears, tool_to_repaired_bugs):
    disj_d4j, inbtw_d4j, same_d4j = [partition & set(multi_chunk_bugs_d4j) for partition in coverage_partitions_d4j]
    ndisj_d4j = inbtw_d4j | same_d4j
    nsame_d4j = disj_d4j | inbtw_d4j
    repairable_d4j, nonrepairable_d4j = partition_bugs_by_repairability(multi_chunk_bugs_d4j, tool_to_repaired_bugs)
    run_chi2(repairable_d4j, nonrepairable_d4j, disj_d4j, ndisj_d4j, \
    "D4J: Chi-squared between disjoint coverage and repairability", \
    'repairable', 'nonrepairable', 'disjoint', 'nondisjoint')
    run_chi2(repairable_d4j, nonrepairable_d4j, same_d4j, nsame_d4j, \
    "D4J: Chi-squared between same coverage and repairability", \
    'repairable', 'nonrepairable', 'same', 'nonsame')
    inbtw_repairable_d4j, inbtw_nonrepairable_d4j = len(inbtw_d4j & repairable_d4j), len(inbtw_d4j & nonrepairable_d4j)
    print("Number of D4J in-between bugs: {} repaired; {} not repaired".format(inbtw_repairable_d4j, inbtw_nonrepairable_d4j))
    print()

    disj_bears, inbtw_bears, same_bears = [partition & set(multi_chunk_bugs_bears) for partition in coverage_partitions_bears]
    ndisj_bears = inbtw_bears | same_bears
    nsame_bears = disj_bears | inbtw_bears
    repairable_bears, nonrepairable_bears = partition_bugs_by_repairability(multi_chunk_bugs_bears, tool_to_repaired_bugs)
    run_fisher_exact(repairable_bears, nonrepairable_bears, disj_bears, ndisj_bears, \
    "Bears: Fisher's Exact Test between disjoint coverage and repairability", \
    'repairable', 'nonrepairable', 'disjoint', 'nondisjoint')
    run_fisher_exact(repairable_bears, nonrepairable_bears, same_bears, nsame_bears, \
    "Bears: Fisher's Exact Test between same coverage and repairability", \
    'repairable', 'nonrepairable', 'same', 'nonsame')
    inbtw_repairable_bears, inbtw_nonrepairable_bears = len(inbtw_bears & repairable_bears), len(inbtw_bears & nonrepairable_bears)
    print("Number of Bears in-between bugs: {} repaired; {} not repaired".format(inbtw_repairable_bears, inbtw_nonrepairable_bears))
    print()

    disj_all, inbtw_all, same_all = disj_d4j | disj_bears, inbtw_d4j | inbtw_bears, same_d4j | same_bears
    ndisj_all = ndisj_d4j | ndisj_bears
    nsame_all = nsame_d4j | nsame_bears
    repairable_all, nonrepairable_all = repairable_d4j | repairable_bears, nonrepairable_d4j | nonrepairable_bears
    run_chi2(repairable_all, nonrepairable_all, disj_all, ndisj_all, \
    "Combined D4J|Bears: Chi-squared between disjoint coverage and repairability", \
    'repairable', 'nonrepairable', 'disjoint', 'nondisjoint')
    run_chi2(repairable_all, nonrepairable_all, same_all, nsame_all, \
    "Combined D4J|Bears: Chi-squared between same coverage and repairability", \
    'repairable', 'nonrepairable', 'same', 'nonsame')
    inbtw_repairable_all, inbtw_nonrepairable_all = len(inbtw_all & repairable_all), len(inbtw_all & nonrepairable_all)
    print("Number of all in-between bugs: {} repaired; {} not repaired".format(inbtw_repairable_all, inbtw_nonrepairable_all))
    print()

if __name__=='__main__':
    multi_edit_bugs_d4j, multi_edit_bugs_bears = get_multi_edit_bugs() #lists of multi-edit bugs
    multi_chunk_bugs_d4j, multi_chunk_bugs_bears = get_multi_chunk_bugs()
    dependencies = get_dependencies() #maps bugId -> dependency 6-tuple
    tool_to_repaired_bugs = get_tool_to_repaired_bugs()
    coverage_partitions_d4j, coverage_partitions_bears = get_coverage_d4j(), get_coverage_bears()
    symptoms_to_bugs_aonly = get_symptoms('asserts_only')
    symptoms_to_bugs_g1 = get_symptoms('grouping1')
    symptoms_to_bugs_g2 = get_symptoms('grouping2')
    print_dependency_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies)
    print_repairability_stats(multi_edit_bugs_d4j, multi_edit_bugs_bears, tool_to_repaired_bugs)
    test_dependency_and_repairability(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies, tool_to_repaired_bugs)
    test_coverage_and_repairability(multi_chunk_bugs_d4j, multi_chunk_bugs_bears, coverage_partitions_d4j, coverage_partitions_bears, tool_to_repaired_bugs)
