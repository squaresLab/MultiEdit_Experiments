import json
import csv
from scipy import stats

DEPENDENCY_TUPLE_INDEX_CTRL=0
DEPENDENCY_TUPLE_INDEX_FLOW=1
DEPENDENCY_TUPLE_INDEX_ANTI=2
DEPENDENCY_TUPLE_INDEX_OUTPUT=3
DEPENDENCY_TUPLE_INDEX_DATA=4
DEPENDENCY_TUPLE_INDEX_ANY=5

bears_single_module_bugs = list(range(1,141+1)) + [143,184,185,188,189,190,191,192,194,198, \
    201,202,204,207,209,210,213,215,216,217,218,219,220,221,223,224,225,226,230, \
    231,232,234,235,238,239,243,244,245,246,247,249,250,251]

def get_bears_proj(bugId):
    fxml_name = 'FasterXML-jackson-databind'
    fxml_nums = set(range(1, 26+1))
    inria_name = 'INRIA-Spoon'
    inria_nums = set(range(27, 83+1)) | set(range(215, 219+1))
    spring_name = 'Spring-data-commons'
    spring_nums = set(range(84, 97+1)) | {244}
    traccar_name = 'Traccar-traccar'
    traccar_nums = set(range(98, 139+1))
    other_name = 'Other-Bears'

    bugnum = int(bugId.split('-')[1])
    if bugnum in fxml_nums:
        return fxml_name
    elif bugnum in inria_nums:
        return inria_name
    elif bugnum in spring_nums:
        return spring_name
    elif bugnum in traccar_nums:
        return traccar_name
    else:
        return other_name

def get_d4j_proj(bugId):
    if 'Chart' in bugId:
        return 'Chart'
    elif 'Closure' in bugId:
        return 'Closure'
    elif 'Lang' in bugId:
        return 'Lang'
    elif 'Math' in bugId:
        return 'Math'
    elif 'Mockito' in bugId:
        return 'Mockito'
    elif 'Time' in bugId:
        return 'Time'
    else:
        raise ValueError('WTF is this bugID: {}'.format(bugId))

def get_all_d4j_bugs():
    d4j_bugs = list()
    d4j_bugs += ['Chart{}'.format(n) for n in range(1, 26+1)]
    d4j_bugs += ['Closure{}'.format(n) for n in range(1, 133+1)]
    d4j_bugs += ['Lang{}'.format(n) for n in range(1, 65+1)]
    d4j_bugs += ['Math{}'.format(n) for n in range(1, 106+1)]
    d4j_bugs += ['Mockito{}'.format(n) for n in range(1, 38+1)]
    d4j_bugs += ['Time{}'.format(n) for n in range(1, 27+1)]
    return set(d4j_bugs)

def get_all_bears_bugs(): #all = all single-module bugs
    return {'Bears-{}'.format(n) for n in bears_single_module_bugs}

def get_multi_line_bugs():
    all_bugs = get_all_d4j_bugs() | get_all_bears_bugs()

    with open('patch_locs.json') as f:
        patch_locs_json = json.load(f)

    multi_edit_bugs_d4j, multi_edit_bugs_bears = list(), list()

    for entry in patch_locs_json:
        bugId = str(entry['bugId']).replace(' ', '') #Remove the space in D4J bugIds
        patch = entry['patch']
        lines_edited = sum(len(edits) for edits in patch.values())
        if bugId in all_bugs and lines_edited > 1:
            if bugId[:5] == 'Bears':
                multi_edit_bugs_bears.append(bugId)
            else:
                multi_edit_bugs_d4j.append(bugId)

    return set(multi_edit_bugs_d4j), set(multi_edit_bugs_bears)

def get_bugs_by_file_distribution():
    all_bugs = get_all_d4j_bugs() | get_all_bears_bugs()
    with open('patch_locs.json') as f:
        patch_locs_json = json.load(f)

    same_file_bugs = set()

    for entry in patch_locs_json:
        bugId = str(entry['bugId']).replace(' ', '') #Remove the space in D4J bugIds
        if bugId in all_bugs: #skip multi-module Bears bugs
            patch = entry['patch'] #dict of files to lines changed by file
            num_files_edited = len(patch.keys())
            if num_files_edited == 1:
                same_file_bugs.add(bugId)

    multi_file_bugs = all_bugs - same_file_bugs

    return same_file_bugs, multi_file_bugs

def get_bugs_by_method_distribution():
    all_bugs = get_all_d4j_bugs() | get_all_bears_bugs()

    multi_method_bugs = set()

    with open('multi-method-bugs.data') as f:
        for line in f:
            bugId = line.strip()
            if bugId in all_bugs:
                multi_method_bugs.add(bugId)

    same_method_bugs = all_bugs - multi_method_bugs

    return same_method_bugs, multi_method_bugs

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
    all_bugs = get_all_d4j_bugs() | get_all_bears_bugs()
    multi_chunk_bugs_d4j = list()
    with open('multi-chunk-bugs/multi_chunk_d4j.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_d4j(line)
            if bugId in all_bugs:
                multi_chunk_bugs_d4j.append(bugId)

    multi_chunk_bugs_bears = list()
    with open('multi-chunk-bugs/multi_chunk_bears.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_bears(line)
            if bugId in all_bugs:
                multi_chunk_bugs_bears.append(bugId)
            else: #debug
                print(bugId)

    return set(multi_chunk_bugs_d4j), set(multi_chunk_bugs_bears)

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

#variant is a bad name; it's really a partial repair instead
def get_has_one_neg_variant():
    has_one_neg_variant_d4j = set()
    with open('partial-repair/d4j/has-one-neg-variant.data') as f:
        for line in f:
            bugId = line.strip()
            has_one_neg_variant_d4j.add(bugId)

    has_one_neg_variant_bears = set()
    with open('partial-repair/bears/has-one-neg-variant.data') as f:
        for line in f:
            bugId = line.strip()
            has_one_neg_variant_bears.add(bugId)

    return has_one_neg_variant_d4j, has_one_neg_variant_bears

def get_pos_neg_neu_proportions():
    proportions = dict() #bugId -> (pos, neg, neu proportions of partial repairs)

    for dataset in ['d4j', 'bears']:
        with open('partial-repair/{}/pos-neg-neu-proportions.csv'.format(dataset)) as f:
            reader=csv.reader(f)
            for row in reader:
                bugId = row[0]
                bugId_proportions = row[1:3]
                proportions[bugId] = bugId_proportions

    return proportions

def get_multi_test_bugs():
    multi_test_bugs = set()

    with open('multi-test-bugs/multi-test-d4j.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_d4j(line)
            multi_test_bugs.add(bugId)

    with open('multi-test-bugs/multi-test-bears.data') as f:
        for line in f:
            bugId = get_bugId_from_Serenas_data_line_bears(line)
            multi_test_bugs.add(bugId)

    return multi_test_bugs

def percent(n_part, n_whole):
    p = 100 * n_part/n_whole
    p = round(p)
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

def run_chi2(r0set, r1set, c0set, c1set, message, r0, r1, c0, c1):
    contingency_table = [[len(r0set & c0set), len(r0set & c1set)],
                         [len(r1set & c0set), len(r1set & c1set)]]

    print(message)
    print('\t{}\t{}'.format(c0, c1))
    print('{}\t{}\t{}'.format(r0, contingency_table[0][0], contingency_table[0][1]))
    print('{}\t{}\t{}'.format(r1, contingency_table[1][0], contingency_table[1][1]))

    chi2, pval, df = stats.chi2_contingency(contingency_table)[0:3]
    print('p-value:', pval)
    print()

def run_fisher_exact(r0set, r1set, c0set, c1set, message, r0, r1, c0, c1):
    contingency_table = [[len(r0set & c0set), len(r0set & c1set)],
                         [len(r1set & c0set), len(r1set & c1set)]]

    print(message)
    print('\t{}\t{}'.format(c0, c1))
    print('{}\t{}\t{}'.format(r0, contingency_table[0][0], contingency_table[0][1]))
    print('{}\t{}\t{}'.format(r1, contingency_table[1][0], contingency_table[1][1]))

    oddsratio, pval = stats.fisher_exact(contingency_table)
    print('p-value:', pval)
    print()

def should_use_fisher_exact(contingency_table):
    for row in contingency_table:
        for elem in row:
            if elem < 5:
                return True
    return False

def run_contingency_analysis(r0set, r1set, c0set, c1set, message, r0, r1, c0, c1):
    r0c0, r0c1 = len(r0set & c0set), len(r0set & c1set)
    r1c0, r1c1 = len(r1set & c0set), len(r1set & c1set)

    r0sum = r0c0 + r0c1
    r1sum = r1c0 + r1c1
    c0sum = r0c0 + r1c0
    c1sum = r0c1 + r1c1
    allsum = r0sum + r1sum

    contingency_table = [[r0c0, r0c1],
                         [r1c0, r1c1]]

    print(message)
    print('\t{}\t{}\ttotal'.format(c0, c1))
    print('{}\t{}\t{}\t{}'.format(r0, contingency_table[0][0], contingency_table[0][1], r0sum))
    print('{}\t{}\t{}\t{}'.format(r1, contingency_table[1][0], contingency_table[1][1], r1sum))
    print('total\t{}\t{}\t{}'.format(c0sum, c1sum, allsum))

    if not is_table_analyzable(contingency_table):
        print("Not analyzable")
        pval = -1
    elif should_use_fisher_exact(contingency_table):
        print("Running Fisher's Exact Test")
        oddsratio, pval = stats.fisher_exact(contingency_table)
    else:
        print("Running Chi-squared")
        chi2, pval, df = stats.chi2_contingency(contingency_table)[0:3]

    print('p-value:', pval)
    print()

def print_repairability_stats(all_bugs, multi_line_bugs, multi_chunk_bugs, tool_to_repaired_bugs):
    single_line_bugs = all_bugs - multi_line_bugs
    single_chunk_bugs = all_bugs - multi_chunk_bugs
    repairable, nonrepairable = partition_bugs_by_repairability(all_bugs, tool_to_repaired_bugs)

    run_contingency_analysis(repairable, nonrepairable, single_line_bugs, multi_line_bugs,
        "Line edits and Repairability",
        'repairable', 'nonrepairable', '1-line', 'multiline')

    run_contingency_analysis(repairable, nonrepairable, single_chunk_bugs, multi_chunk_bugs,
        "Chunk edits and Repairability",
        'repairable', 'nonrepairable', '1-chunk', 'multichunk')

def test_dependency_and_repairability(multi_edit_bugs_d4j, multi_edit_bugs_bears, dependencies, tool_to_repaired_bugs):
    ctrl_dependent_d4j, ctrl_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_d4j, data_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_d4j, any_nondependent_d4j = partition_bugs_by_dependency(multi_edit_bugs_d4j, dependencies, DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_d4j, nonrepairable_d4j = partition_bugs_by_repairability(multi_edit_bugs_d4j, tool_to_repaired_bugs)

    run_contingency_analysis(repairable_d4j, nonrepairable_d4j, ctrl_dependent_d4j, ctrl_nondependent_d4j, \
    "D4J: control dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_d4j, nonrepairable_d4j, data_dependent_d4j, data_nondependent_d4j, \
    "D4J: data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_d4j, nonrepairable_d4j, any_dependent_d4j, any_nondependent_d4j, \
    "D4J: control|data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

    ctrl_dependent_bears, ctrl_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_bears, data_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_bears, any_nondependent_bears = partition_bugs_by_dependency(multi_edit_bugs_bears, dependencies, DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_bears, nonrepairable_bears = partition_bugs_by_repairability(multi_edit_bugs_bears, tool_to_repaired_bugs)

    run_contingency_analysis(repairable_bears, nonrepairable_bears, ctrl_dependent_bears, ctrl_nondependent_bears, \
    "Bears: between control dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_bears, nonrepairable_bears, data_dependent_bears, data_nondependent_bears, \
    "Bears: between data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_bears, nonrepairable_bears, any_dependent_bears, any_nondependent_bears, \
    "Bears: between control|data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

    ctrl_dependent_all, ctrl_nondependent_all = ctrl_dependent_d4j | ctrl_dependent_bears, ctrl_nondependent_d4j | ctrl_nondependent_bears
    data_dependent_all, data_nondependent_all = data_dependent_d4j | data_dependent_bears, data_nondependent_d4j | data_nondependent_bears
    any_dependent_all, any_nondependent_all = any_dependent_d4j | any_dependent_bears, any_nondependent_d4j | any_nondependent_bears
    repairable_all, nonrepairable_all = repairable_d4j | repairable_bears, nonrepairable_d4j | nonrepairable_bears

    run_contingency_analysis(repairable_all, nonrepairable_all, ctrl_dependent_all, ctrl_nondependent_all, \
    "Combined D4J|Bears: control dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_all, nonrepairable_all, data_dependent_all, data_nondependent_all, \
    "Combined D4J|Bears: data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_all, nonrepairable_all, any_dependent_all, any_nondependent_all, \
    "Combined D4J|Bears: control|data dependency and repairability", \
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

def test_coverage_and_repairability(multi_chunk_bugs_d4j, multi_chunk_bugs_bears, coverage_partitions_d4j, coverage_partitions_bears, tool_to_repaired_bugs):
    disj_d4j, inbtw_d4j, same_d4j = [partition & set(multi_chunk_bugs_d4j) for partition in coverage_partitions_d4j]
    ndisj_d4j = inbtw_d4j | same_d4j
    nsame_d4j = disj_d4j | inbtw_d4j
    repairable_d4j, nonrepairable_d4j = partition_bugs_by_repairability(multi_chunk_bugs_d4j, tool_to_repaired_bugs)
    run_contingency_analysis(repairable_d4j, nonrepairable_d4j, disj_d4j, ndisj_d4j, \
    "D4J: disjoint coverage and repairability", \
    'repairable', 'nonrepairable', 'disjoint', 'nondisjoint')
    run_contingency_analysis(repairable_d4j, nonrepairable_d4j, same_d4j, nsame_d4j, \
    "D4J: same coverage and repairability", \
    'repairable', 'nonrepairable', 'same', 'nonsame')
    inbtw_repairable_d4j, inbtw_nonrepairable_d4j = len(inbtw_d4j & repairable_d4j), len(inbtw_d4j & nonrepairable_d4j)
    print("Number of D4J in-between bugs: {} repaired; {} not repaired".format(inbtw_repairable_d4j, inbtw_nonrepairable_d4j))
    print()

    disj_bears, inbtw_bears, same_bears = [partition & set(multi_chunk_bugs_bears) for partition in coverage_partitions_bears]
    ndisj_bears = inbtw_bears | same_bears
    nsame_bears = disj_bears | inbtw_bears
    repairable_bears, nonrepairable_bears = partition_bugs_by_repairability(multi_chunk_bugs_bears, tool_to_repaired_bugs)
    run_contingency_analysis(repairable_bears, nonrepairable_bears, disj_bears, ndisj_bears, \
    "Bears: between disjoint coverage and repairability", \
    'repairable', 'nonrepairable', 'disjoint', 'nondisjoint')
    run_contingency_analysis(repairable_bears, nonrepairable_bears, same_bears, nsame_bears, \
    "Bears: between same coverage and repairability", \
    'repairable', 'nonrepairable', 'same', 'nonsame')
    inbtw_repairable_bears, inbtw_nonrepairable_bears = len(inbtw_bears & repairable_bears), len(inbtw_bears & nonrepairable_bears)
    print("Number of Bears in-between bugs: {} repaired; {} not repaired".format(inbtw_repairable_bears, inbtw_nonrepairable_bears))
    print()

    disj_all, inbtw_all, same_all = disj_d4j | disj_bears, inbtw_d4j | inbtw_bears, same_d4j | same_bears
    ndisj_all = ndisj_d4j | ndisj_bears
    nsame_all = nsame_d4j | nsame_bears
    repairable_all, nonrepairable_all = repairable_d4j | repairable_bears, nonrepairable_d4j | nonrepairable_bears
    run_contingency_analysis(repairable_all, nonrepairable_all, disj_all, ndisj_all, \
    "Combined D4J|Bears: disjoint coverage and repairability", \
    'repairable', 'nonrepairable', 'disjoint', 'nondisjoint')
    run_contingency_analysis(repairable_all, nonrepairable_all, same_all, nsame_all, \
    "Combined D4J|Bears: same coverage and repairability", \
    'repairable', 'nonrepairable', 'same', 'nonsame')
    inbtw_repairable_all, inbtw_nonrepairable_all = len(inbtw_all & repairable_all), len(inbtw_all & nonrepairable_all)
    print("Number of all in-between bugs: {} repaired; {} not repaired".format(inbtw_repairable_all, inbtw_nonrepairable_all))
    print()

def is_table_analyzable(contingency_table):
    #check all rows for 0s
    for row in contingency_table:
        found_nonzero_elem_in_row = False
        for elem in row:
            if elem != 0:
                found_nonzero_elem_in_row = True
                break
        if not found_nonzero_elem_in_row:
            return False

    #check all cols for 0s
    for colnum in range(len(contingency_table[0])):
        found_nonzero_elem_in_col = False
        for rownum in range(len(contingency_table)):
            elem = contingency_table[rownum][colnum]
            if elem != 0:
                found_nonzero_elem_in_col = True
                break
        if not found_nonzero_elem_in_col:
            return False

    #no row or column is full of zeroes
    return True

def test_symptoms_and_repairability(bugs_d4j, bugs_bears, symptoms_to_bugs, grouping_name, tool_to_repaired_bugs):
    bugsets = [(set(bugs_d4j), 'D4J'), (set(bugs_bears), 'Bears'), (set(bugs_d4j + bugs_bears), 'Combined D4J|Bears')]
    for bugset, bugset_name in bugsets:
        repairable, nonrepairable = partition_bugs_by_repairability(bugset, tool_to_repaired_bugs)
        for symptom in symptoms_to_bugs.keys():
            symptomatic = bugset & symptoms_to_bugs[symptom]
            asymptomatic = bugset - symptomatic
            run_contingency_analysis(repairable, nonrepairable, symptomatic, asymptomatic, \
            "{} on grouping {}: Symptom <{}> and repairability".format(bugset_name, grouping_name, symptom), \
            'repairable', 'nonrepairable', 'symptomatic', 'asymptomatic')

def test_neg_variant_and_same_coverage(bugs_d4j, bugs_bears, \
                                        one_neg_var_d4j, one_neg_var_bears, \
                                        cov_partitions_d4j, cov_partitions_bears):
    no_neg_var_d4j = set(bugs_d4j) - one_neg_var_d4j
    disj_d4j, inbtw_d4j, same_d4j = [partition & set(bugs_d4j) for partition in cov_partitions_d4j]
    nsame_d4j = disj_d4j | inbtw_d4j
    run_contingency_analysis(one_neg_var_d4j, no_neg_var_d4j, same_d4j, nsame_d4j, \
            "D4J: At least one negative partial repair and same coverage", \
            'one negative', 'none negative', 'Same', 'non-Same')

    no_neg_var_bears = set(bugs_bears) - one_neg_var_bears
    disj_bears, inbtw_bears, same_bears = [partition & set(bugs_bears) for partition in cov_partitions_bears]
    nsame_bears = disj_bears | inbtw_bears
    run_contingency_analysis(one_neg_var_bears, no_neg_var_bears, same_bears, nsame_bears, \
            "Bears: At least one negative partial repair and same coverage", \
            'one negative', 'none negative', 'Same', 'non-Same')

    one_neg_var_combined = one_neg_var_d4j | one_neg_var_bears
    no_neg_var_combined = no_neg_var_d4j | no_neg_var_bears
    same_combined = same_d4j | same_bears
    nsame_combined = nsame_d4j | nsame_bears
    run_contingency_analysis(one_neg_var_combined, no_neg_var_combined, same_combined, nsame_combined, \
            "Combined D4J|Bears: At least one negative partial repair and same coverage", \
            'one negative', 'none negative', 'Same', 'non-Same')

def print_edit_and_test_info(all_bugs, multi_edit_bugs, multi_test_bugs):
    single_edit_bugs = all_bugs - multi_edit_bugs
    single_test_bugs = all_bugs - multi_test_bugs
    run_contingency_analysis(single_test_bugs, multi_test_bugs, single_edit_bugs, multi_edit_bugs, \
            "Num edits and num tests",
            'single test', 'multi-tests', 'single chunk', 'multi-chunk')

def partition_bugs_by_project(bugs):
    proj_to_bugs = dict()

    for bugId in bugs:
        if 'Bears' in bugId:
            proj = get_bears_proj(bugId)
        else:
            proj = get_d4j_proj(bugId)

        if proj not in proj_to_bugs.keys():
            proj_to_bugs[proj] = set()

        proj_to_bugs[proj].add(bugId)

    return proj_to_bugs

def get_multi_edit_frequencies_per_proj():
    all_bugs = get_all_d4j_bugs() | get_all_bears_bugs()
    multi_line_bugs_d4j, multi_line_bugs_bears = get_multi_line_bugs()
    multi_line_bugs = multi_line_bugs_d4j | multi_line_bugs_bears
    multi_chunk_bugs_d4j, multi_chunk_bugs_bears = get_multi_chunk_bugs()
    multi_chunk_bugs = multi_chunk_bugs_d4j | multi_chunk_bugs_bears
    multi_test_bugs = get_multi_test_bugs()
    mchunk_mtest_bugs = multi_chunk_bugs & multi_test_bugs #intersection

    proj_to_bugs = partition_bugs_by_project(all_bugs)
    proj_to_multi_line_bugs = partition_bugs_by_project(multi_line_bugs)
    proj_to_multi_chunk_bugs = partition_bugs_by_project(multi_chunk_bugs)
    proj_to_multi_test_bugs = partition_bugs_by_project(multi_test_bugs)
    proj_to_mtest_mchunk_bugs = partition_bugs_by_project(mchunk_mtest_bugs)

    print("Proj\tTotal\tMLine\tMChunk\tMTest\tMTestMChunk")
    for proj in proj_to_bugs.keys():
        total = len(proj_to_bugs[proj])
        mline = len(proj_to_multi_line_bugs[proj])
        p_mline = percent(mline, total)
        mchunk = len(proj_to_multi_chunk_bugs[proj])
        p_mchunk = percent(mchunk, total)
        mtest = len(proj_to_multi_test_bugs[proj])
        p_mtest = percent(mtest, total)
        mtestmchunk = len(proj_to_mtest_mchunk_bugs[proj])
        p_mtestmchunk = percent(mtestmchunk, total)
        print('{}\t{}\t{}({})\t{}({})\t{}({})\t{}({})' \
                .format(proj, total, mline, p_mline, mchunk, p_mchunk, mtest, p_mtest, mtestmchunk, p_mtestmchunk))

if __name__=='__main__':
    #get_multi_edit_frequencies_per_proj()
    print(len(get_multi_chunk_bugs()[1]))

    #all_bugs_d4j, all_bugs_bears = get_all_d4j_bugs(), get_all_bears_bugs()
    #multi_line_bugs_d4j, multi_line_bugs_bears = get_multi_line_bugs()
    #multi_chunk_bugs_d4j, multi_chunk_bugs_bears = get_multi_chunk_bugs()
    #dependencies = get_dependencies() #maps bugId -> dependency 6-tuple
    #tool_to_repaired_bugs = get_tool_to_repaired_bugs()
    #coverage_partitions_d4j, coverage_partitions_bears = get_coverage_d4j(), get_coverage_bears()
    #symptoms_to_bugs_aonly = get_symptoms('asserts_only')
    #symptoms_to_bugs_g1 = get_symptoms('grouping1')
    #symptoms_to_bugs_g2 = get_symptoms('grouping2')
    #has_one_neg_variant_d4j, has_one_neg_variant_bears = get_has_one_neg_variant()
    #pos_neg_neu_proportions = get_pos_neg_neu_proportions()
    #multi_test_bugs = get_multi_test_bugs()


    #print(len(all_bugs_bears - multi_line_bugs_bears))
    #print("D4J repairability:")
    #print_repairability_stats(all_bugs_d4j, multi_line_bugs_d4j, multi_chunk_bugs_d4j, tool_to_repaired_bugs)
    #print("Bears repairability:")
    #print_repairability_stats(all_bugs_bears, multi_line_bugs_bears, multi_chunk_bugs_bears, tool_to_repaired_bugs)
    #print("Combined repairability:")
    #print_repairability_stats(all_bugs_d4j|all_bugs_bears, multi_line_bugs_d4j|multi_line_bugs_bears, multi_chunk_bugs_d4j|multi_chunk_bugs_bears, tool_to_repaired_bugs)

    #print("D4J chunks and tests:")
    #print_edit_and_test_info(all_bugs_d4j, multi_chunk_bugs_d4j, multi_test_bugs)
    #print("Bears chunks and tests:")
    #print_edit_and_test_info(all_bugs_bears, multi_chunk_bugs_bears, multi_test_bugs)
    #print("Combined chunks and tests:")
    #print_edit_and_test_info(all_bugs_d4j|all_bugs_bears, multi_chunk_bugs_d4j|multi_chunk_bugs_bears, multi_test_bugs)

    #print(len(multi_chunk_bugs_d4j), len(multi_chunk_bugs_bears))
    #print_dependency_stats(multi_line_bugs_d4j, multi_line_bugs_bears, dependencies)
    #test_dependency_and_repairability(multi_line_bugs_d4j, multi_line_bugs_bears, dependencies, tool_to_repaired_bugs)
    #test_coverage_and_repairability(multi_line_bugs_d4j, multi_line_bugs_bears, coverage_partitions_d4j, coverage_partitions_bears, tool_to_repaired_bugs)
    #test_symptoms_and_repairability(multi_line_bugs_d4j, multi_line_bugs_bears, symptoms_to_bugs_aonly, 'asserts_only', tool_to_repaired_bugs)
    #test_symptoms_and_repairability(multi_line_bugs_d4j, multi_line_bugs_bears, symptoms_to_bugs_g1, 'grouping1', tool_to_repaired_bugs)
    #test_symptoms_and_repairability(multi_line_bugs_d4j, multi_line_bugs_bears, symptoms_to_bugs_g2, 'grouping2', tool_to_repaired_bugs)
    #test_neg_variant_and_same_coverage(multi_chunk_bugs_d4j, multi_chunk_bugs_bears, has_one_neg_variant_d4j, has_one_neg_variant_bears, coverage_partitions_d4j, coverage_partitions_bears)
