import json
import csv
import re
import statistics
from itertools import chain

from typing import Set, List, Dict, Tuple, Collection

from scipy import stats

BugId = str
Project = str
APRTool = str

DEPENDENCY_TUPLE_INDEX_CTRL=0
DEPENDENCY_TUPLE_INDEX_FLOW=1
DEPENDENCY_TUPLE_INDEX_ANTI=2
DEPENDENCY_TUPLE_INDEX_OUTPUT=3
DEPENDENCY_TUPLE_INDEX_DATA=4
DEPENDENCY_TUPLE_INDEX_ANY=5

bears_single_module_bugs = list(range(1,141+1)) + [143,184,185,188,189,190,191,192,194,198, \
    201,202,204,207,209,210,213,215,216,217,218,219,220,221,223,224,225,226,230, \
    231,232,234,235,238,239,243,244,245,246,247,249,250,251]


def percent(n_part: int, n_whole: int) -> str:
    p = 100 * n_part/n_whole
    p = round(p)
    return "{}%".format(p)

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

def get_bears_proj(bugId) -> Project:
    fxml_name = 'FasterXML-jackson-databind'
    fxml_nums = set(range(1, 26+1))
    inria_name = 'INRIA-Spoon'
    inria_nums = set(range(27, 83+1)) | set(range(215, 219+1))
    spring_name = 'Spring-data-commons'
    spring_nums = set(range(84, 97+1)) | {244}
    traccar_name = 'Traccar-traccar'
    traccar_nums = set(range(98, 139+1))
    other_name = 'Other-Bears'

    bugnum = int(bugId[5:])
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

def get_d4j_proj(bugId: BugId) -> Project:
    valid_d4j_projects = ['Chart', 'Cli', 'Closure', 'Codec', 'Collections', 'Compress', 'Csv', 'Gson', 'Jacksoncore',
                       'Jacksondatabind', 'Jacksonxml', 'Jsoup', 'Jxpath', 'Lang', 'Math', 'Mockito', 'Time']

    for d4j_proj in valid_d4j_projects:
        if d4j_proj in bugId:
            return d4j_proj

    raise ValueError('WTF is this bugID: {}'.format(bugId))

def get_proj(bugId: BugId) -> Project:
    if 'Bears' in bugId:
        return get_bears_proj(bugId)
    else:
        return get_d4j_proj(bugId)

def get_all_d4j_bugs() -> Set[BugId]:
    d4j_bugs = list()
    d4j_bugs += ['Chart{}'.format(n) for n in range(1, 26+1)]
    d4j_bugs += ['Cli{}'.format(n) for n in range(1, 5+1)]
    d4j_bugs += ['Cli{}'.format(n) for n in range(7, 40+1)]
    d4j_bugs += ['Closure{}'.format(n) for n in range(1, 62+1)]
    d4j_bugs += ['Closure{}'.format(n) for n in range(64, 92+1)]
    d4j_bugs += ['Closure{}'.format(n) for n in range(94, 176+1)]
    d4j_bugs += ['Codec{}'.format(n) for n in range(1, 18+1)]
    d4j_bugs += ['Collections{}'.format(n) for n in range(25, 28+1)]
    d4j_bugs += ['Compress{}'.format(n) for n in range(1, 47+1)]
    d4j_bugs += ['Csv{}'.format(n) for n in range(1, 16+1)]
    d4j_bugs += ['Gson{}'.format(n) for n in range(1, 18+1)]
    d4j_bugs += ['Jacksoncore{}'.format(n) for n in range(1, 26+1)]
    d4j_bugs += ['Jacksondatabind{}'.format(n) for n in range(1, 112+1)]
    d4j_bugs += ['Jacksonxml{}'.format(n) for n in range(1, 6+1)]
    d4j_bugs += ['Jsoup{}'.format(n) for n in range(1, 93+1)]
    d4j_bugs += ['Jxpath{}'.format(n) for n in range(1, 22+1)]
    d4j_bugs += ['Lang{}'.format(n) for n in range(1, 1+1)]
    d4j_bugs += ['Lang{}'.format(n) for n in range(3, 65+1)]
    d4j_bugs += ['Math{}'.format(n) for n in range(1, 106+1)]
    d4j_bugs += ['Mockito{}'.format(n) for n in range(1, 38+1)]
    d4j_bugs += ['Time{}'.format(n) for n in range(1, 20+1)]
    d4j_bugs += ['Time{}'.format(n) for n in range(22, 27+1)]
    return set(d4j_bugs)

def get_all_bears_bugs() -> Set[BugId]: #all = all single-module bugs
    return {'Bears{}'.format(n) for n in bears_single_module_bugs}

def is_evaluated_by_RepairThemAll(bugId: str) -> bool:
    """
    False if the bug is newly added in D4J 2.0.0; True otherwise.
    """
    if 'Bears' in bugId or 'Chart' in bugId or 'Lang' in bugId \
        or 'Math' in bugId or 'Mockito' in bugId or 'Time' in bugId:
        return True
    elif 'Closure' in bugId:
        if len(bugId) < 10: return True
        elif len(bugId) == 10 and int(bugId[-3:]) <= 133: return True
        else: return False
    else:
        return False


proj_pattern = re.compile('^([A-Za-z]+)')
bugnum_pattern = re.compile('([0-9]+)$')

def normalize_bugId(not_normalized_bugId: str) -> BugId:
    """
    Normalize bugIds to a standard format in the style of: Chart32.
    :arg not_normalized_bugId: a non-normalized bugId.
        Supports the non-normalized styles of: Chart32, CHART:032, CHART:32, Chart-32, CHART-32.
    :return: Normalized bugId in the style of: Chart32.
    """
    not_normalized_bugId = not_normalized_bugId.strip()

    try:
        proj_raw = proj_pattern.search(not_normalized_bugId).group(1)
        bugnum_raw = bugnum_pattern.search(not_normalized_bugId).group(1)
    except AttributeError:
        raise Exception(f'Cannot interpret: {not_normalized_bugId}')

    proj = proj_raw[0].upper() + proj_raw[1:].lower()
    bugnum = int(bugnum_raw)

    if proj == 'Clojure':
        proj = 'Closure'

    normalized_bugId = proj + str(bugnum)
    return normalized_bugId

def get_bug_list(path_to_bug_list_file: str) -> List[BugId]:
    bug_list = list()

    with open(path_to_bug_list_file) as f:
        for line in f:
            if len(line.strip()) == 0:
                continue #ignore empty lines

            not_normalized_bugId = line
            bugId = normalize_bugId(not_normalized_bugId)
            bug_list.append(bugId)

    return bug_list



def get_bugs_by_file_distribution():
    with open('../patch_locs.json') as f:
        patch_locs_json = json.load(f)

    same_file_bugs = set()

    for entry in patch_locs_json:
        bugId = normalize_bugId(str(entry['bugId']))
        if bugId in all_bugs: #skip multi-module Bears or deprecated bugs
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


def get_dependencies() -> Dict[BugId, List[bool]]: #maps bugId -> 6-Tuple info on dependencies
    dependencies = dict()

    with open('../dependency-experiments/combined-results.csv') as f:
        dependencies_reader=csv.reader(f)
        is_header_row = True
        for row in dependencies_reader:
            if is_header_row: #skip the header row
                is_header_row = False
                continue
            bugId = normalize_bugId(row[0])
            dependency_info = [True if v == 'True' else False for v in row[1:]]
            dependencies[bugId] = dependency_info

    return dependencies

def get_repairs_bears() -> Dict[APRTool, List[BugId]]:
    repairs = dict() #maps tool -> bugIds of bugs repaired by the tool

    with open('repair-them-all/Bears.csv') as f:
        reader=csv.reader(f)
        for row in reader:
            tool = row[0]
            bugnums = row[1:] #may be empty
            bugIds = ['Bears{}'.format(bugnum) for bugnum in bugnums]
            repairs[tool] = bugIds

    return repairs

def get_repairs_d4j() -> Dict[APRTool, List[BugId]]:
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

def get_tool_to_repaired_bugs() -> Dict[APRTool, List[BugId]]:
    repairs_d4j = get_repairs_d4j()
    repairs_bears = get_repairs_bears()
    tools = repairs_d4j.keys()

    tools_to_repaired_bugs = dict()
    for tool in tools:
        tools_to_repaired_bugs[tool] = repairs_d4j[tool] + repairs_bears[tool]

    return tools_to_repaired_bugs

def partition_bugs_by_project(bugs: Collection[BugId]) -> Dict[str, Set[BugId]]:
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

def get_bug_to_num_changed_lines() -> Dict[BugId, int]:
    with open('../patch_locs.json') as f:
        patch_locs_json = json.load(f)

    bug_to_num_changed_lines: Dict[BugId, int] = dict()
    for entry in patch_locs_json:
        bugId = normalize_bugId(str(entry['bugId']))
        num_lines = sum(len(changed_lines) for changed_lines in entry["patch"].values())
        bug_to_num_changed_lines[bugId] = num_lines

    return bug_to_num_changed_lines


######################################################################################################


all_d4j_bugs: Set[BugId]  = get_all_d4j_bugs()
all_bears_bugs: Set[BugId]  = get_all_bears_bugs()
all_bugs: Set[BugId]  = all_d4j_bugs | all_bears_bugs

multi_line_bugs_d4j: Set[BugId] = all_bugs & set(get_bug_list('../multi-line-bugs/multi_line_d4j.data'))
multi_line_bugs_bears: Set[BugId]  = all_bugs & set(get_bug_list('../multi-line-bugs/multi_line_bears.data'))
multi_line_bugs: Set[BugId]  = multi_line_bugs_d4j | multi_line_bugs_bears

multi_location_bugs_d4j: Set[BugId] = all_bugs & set(get_bug_list('../multi-location-bugs/multi_location_d4j.data'))
multi_location_bugs_bears: Set[BugId] = all_bugs & set(get_bug_list('../multi-location-bugs/multi_location_bears.data'))
multi_location_bugs = multi_location_bugs_d4j | multi_location_bugs_bears

multi_test_bugs: Set[BugId] = all_bugs & set(get_bug_list('../more_than_one_test.txt'))
mlocation_mtest_bugs: Set[BugId] = all_bugs & set(get_bug_list('../multitest_multiedit.txt'))

dependencies: Dict[BugId, Tuple[bool, bool, bool, bool, bool, bool]] = get_dependencies()

tool_to_repaired_bugs: Dict[APRTool, List[BugId]] = get_tool_to_repaired_bugs()

all_repairable_bugs: Set[BugId] = set(chain.from_iterable(tool_to_repaired_bugs.values()))

two_six_location_bugs = set(get_bug_list('../2_6_locs_bugs.data'))


def print_dependency_stats():
    n_total_bugs_d4j = len(get_all_d4j_bugs())
    n_single_edit_bugs_d4j = n_total_bugs_d4j - len(multi_line_bugs_d4j)
    n_multi_edit_bugs_d4j = n_total_bugs_d4j - n_single_edit_bugs_d4j
    n_bugs_with_ctrl_deps_d4j = sum(1 for bug in multi_line_bugs_d4j if bug in dependencies and dependencies[bug][0])
    n_bugs_with_data_deps_d4j = sum(1 for bug in multi_line_bugs_d4j if bug in dependencies and dependencies[bug][4])
    n_bugs_with_any_deps_d4j = sum(1 for bug in multi_line_bugs_d4j if bug in dependencies and dependencies[bug][5])
    n_bugs_with_no_deps_d4j = len(multi_line_bugs_d4j) - n_bugs_with_any_deps_d4j
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

    n_total_bugs_bears = len(get_all_bears_bugs()) #184 single modules - 3 cassandra-reaper bugs that used a multi-module design
    n_single_edit_bugs_bears = n_total_bugs_bears - len(multi_line_bugs_bears)
    n_multi_edit_bugs_bears = n_total_bugs_bears - n_single_edit_bugs_bears
    n_bugs_with_ctrl_deps_bears = sum(1 for bug in multi_line_bugs_bears if bug in dependencies and dependencies[bug][0])
    n_bugs_with_data_deps_bears = sum(1 for bug in multi_line_bugs_bears if bug in dependencies and dependencies[bug][4])
    n_bugs_with_any_deps_bears = sum(1 for bug in multi_line_bugs_bears if bug in dependencies and dependencies[bug][5])
    n_bugs_with_no_deps_bears = len(multi_line_bugs_bears) - n_bugs_with_any_deps_bears
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

def partition_bugs_by_repairability(bugs_to_partition_collection: Collection[BugId]) -> Tuple[Set[BugId], Set[BugId], Set[BugId]]:
    bugs_to_partition_set = set(bugs_to_partition_collection)

    evaluated_partition = set(filter(lambda bugId: is_evaluated_by_RepairThemAll(bugId), bugs_to_partition_collection))
    not_evaluated_partition = bugs_to_partition_set - evaluated_partition

    repairable_paritition = evaluated_partition & all_repairable_bugs
    unrepairable_partition = evaluated_partition - all_repairable_bugs
    return repairable_paritition, unrepairable_partition, not_evaluated_partition

def partition_bugs_by_dependency(bugs_to_partition, dependency_tuple_index) -> Tuple[Set[BugId], Set[BugId]]:
    all_dependent_bugs = set(bug for bug in dependencies if dependencies[bug][dependency_tuple_index])

    bugs_to_partition_set = set(bugs_to_partition)
    dependent_partition = bugs_to_partition_set & all_dependent_bugs
    nondependent_partition = bugs_to_partition_set - all_dependent_bugs
    return dependent_partition, nondependent_partition

def print_repairability_stats(all_bugs, multi_line_bugs, multi_location_bugs):
    single_line_bugs = all_bugs - multi_line_bugs
    single_location_bugs = all_bugs - multi_location_bugs
    repairable, nonrepairable, notevaluated = partition_bugs_by_repairability(all_bugs)

    run_contingency_analysis(repairable, nonrepairable, single_line_bugs, multi_line_bugs,
        "Line edits and Repairability",
        'repairable', 'nonrepairable', '1-line', 'multiline')

    run_contingency_analysis(repairable, nonrepairable, single_location_bugs, multi_location_bugs,
        "Chunk edits and Repairability",
        'repairable', 'nonrepairable', '1-chunk', 'multichunk')

def test_dependency_and_repairability():
    ctrl_dependent_d4j, ctrl_nondependent_d4j = partition_bugs_by_dependency(multi_line_bugs_d4j,
                                                                             DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_d4j, data_nondependent_d4j = partition_bugs_by_dependency(multi_line_bugs_d4j,
                                                                             DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_d4j, any_nondependent_d4j = partition_bugs_by_dependency(multi_line_bugs_d4j,
                                                                           DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_d4j, nonrepairable_d4j, notevaluated_d4j = partition_bugs_by_repairability(multi_line_bugs_d4j)

    print('D4J no evaluation:')
    print(f'\t{len(notevaluated_d4j & any_dependent_d4j)} control|data dependents')
    print(f'\t{len(notevaluated_d4j & any_nondependent_d4j)} nondependents')
    print(f'\t{len(notevaluated_d4j)} total')
    # run_contingency_analysis(repairable_d4j, nonrepairable_d4j, ctrl_dependent_d4j, ctrl_nondependent_d4j,
    # "D4J: control dependency and repairability",
    # 'repairable', 'nonrepairable', 'dependent', 'nondependent')
    # run_contingency_analysis(repairable_d4j, nonrepairable_d4j, data_dependent_d4j, data_nondependent_d4j,
    # "D4J: data dependency and repairability",
    # 'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_d4j, nonrepairable_d4j, any_dependent_d4j, any_nondependent_d4j,
    "D4J: control|data dependency and repairability",
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

    ctrl_dependent_bears, ctrl_nondependent_bears = partition_bugs_by_dependency(multi_line_bugs_bears,
                                                                                 DEPENDENCY_TUPLE_INDEX_CTRL)
    data_dependent_bears, data_nondependent_bears = partition_bugs_by_dependency(multi_line_bugs_bears,
                                                                                 DEPENDENCY_TUPLE_INDEX_DATA)
    any_dependent_bears, any_nondependent_bears = partition_bugs_by_dependency(multi_line_bugs_bears,
                                                                               DEPENDENCY_TUPLE_INDEX_ANY)
    repairable_bears, nonrepairable_bears, notevaluated_bears = partition_bugs_by_repairability(multi_line_bugs_bears)

    # run_contingency_analysis(repairable_bears, nonrepairable_bears, ctrl_dependent_bears, ctrl_nondependent_bears,
    # "Bears: between control dependency and repairability",
    # 'repairable', 'nonrepairable', 'dependent', 'nondependent')
    # run_contingency_analysis(repairable_bears, nonrepairable_bears, data_dependent_bears, data_nondependent_bears,
    # "Bears: between data dependency and repairability",
    # 'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_bears, nonrepairable_bears, any_dependent_bears, any_nondependent_bears,
    "Bears: between control|data dependency and repairability",
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

    ctrl_dependent_all, ctrl_nondependent_all = ctrl_dependent_d4j | ctrl_dependent_bears, ctrl_nondependent_d4j | ctrl_nondependent_bears
    data_dependent_all, data_nondependent_all = data_dependent_d4j | data_dependent_bears, data_nondependent_d4j | data_nondependent_bears
    any_dependent_all, any_nondependent_all = any_dependent_d4j | any_dependent_bears, any_nondependent_d4j | any_nondependent_bears
    repairable_all, nonrepairable_all = repairable_d4j | repairable_bears, nonrepairable_d4j | nonrepairable_bears

    # run_contingency_analysis(repairable_all, nonrepairable_all, ctrl_dependent_all, ctrl_nondependent_all,
    # "Combined D4J|Bears: control dependency and repairability",
    # 'repairable', 'nonrepairable', 'dependent', 'nondependent')
    # run_contingency_analysis(repairable_all, nonrepairable_all, data_dependent_all, data_nondependent_all,
    # "Combined D4J|Bears: data dependency and repairability",
    # 'repairable', 'nonrepairable', 'dependent', 'nondependent')
    run_contingency_analysis(repairable_all, nonrepairable_all, any_dependent_all, any_nondependent_all,
    "Combined D4J|Bears: control|data dependency and repairability",
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

def test_dependency_and_size():
    bug_to_num_changed_lines = get_bug_to_num_changed_lines()
    any_dependent_d4j, any_nondependent_d4j = partition_bugs_by_dependency(multi_line_bugs_d4j,
                                                                           DEPENDENCY_TUPLE_INDEX_ANY)
    any_dependent_bears, any_nondependent_bears = partition_bugs_by_dependency(multi_line_bugs_bears,
                                                                               DEPENDENCY_TUPLE_INDEX_ANY)
    any_dependent_all, any_nondependent_all = any_dependent_d4j | any_dependent_bears, any_nondependent_d4j | any_nondependent_bears
    repairable_d4j, nonrepairable_d4j, notevaluated_d4j = partition_bugs_by_repairability(multi_line_bugs_d4j)
    repairable_bears, nonrepairable_bears, notevaluated_bears = partition_bugs_by_repairability(multi_line_bugs_bears)
    repairable_all, nonrepairable_all = repairable_d4j | repairable_bears, nonrepairable_d4j | nonrepairable_bears

    # restrict consideration to only bugs analyzed for repairability
    any_dependent_all = any_dependent_all & (repairable_all | nonrepairable_all)
    any_nondependent_all = any_nondependent_all & (repairable_all | nonrepairable_all)

    num_changed_lines_dependent_patches = [bug_to_num_changed_lines[bug]
                                           for bug in any_dependent_all]
    num_changed_lines_nondependent_patches = [bug_to_num_changed_lines[bug]
                                              for bug in any_nondependent_all]
    print(statistics.mean(num_changed_lines_dependent_patches),
          statistics.median(num_changed_lines_dependent_patches),
          statistics.stdev(num_changed_lines_dependent_patches))
    print(statistics.mean(num_changed_lines_nondependent_patches),
          statistics.median(num_changed_lines_nondependent_patches),
          statistics.stdev(num_changed_lines_nondependent_patches))
    ustatistic, pvalue = stats.mannwhitneyu(num_changed_lines_dependent_patches, num_changed_lines_nondependent_patches)
    print(pvalue)

    import matplotlib.pyplot as plt
    plt.boxplot([num_changed_lines_dependent_patches, num_changed_lines_nondependent_patches],
                labels=["dependent", "non-dependent"])
    plt.show()

def test_dependency_size_and_repairability():
    bug_to_num_changed_lines = get_bug_to_num_changed_lines()
    any_dependent_d4j, any_nondependent_d4j = partition_bugs_by_dependency(multi_line_bugs_d4j,
                                                                           DEPENDENCY_TUPLE_INDEX_ANY)
    any_dependent_bears, any_nondependent_bears = partition_bugs_by_dependency(multi_line_bugs_bears,
                                                                               DEPENDENCY_TUPLE_INDEX_ANY)
    any_dependent_all, any_nondependent_all = any_dependent_d4j | any_dependent_bears, any_nondependent_d4j | any_nondependent_bears
    repairable_d4j, nonrepairable_d4j, notevaluated_d4j = partition_bugs_by_repairability(multi_line_bugs_d4j)
    repairable_bears, nonrepairable_bears, notevaluated_bears = partition_bugs_by_repairability(multi_line_bugs_bears)
    repairable_all, nonrepairable_all = repairable_d4j | repairable_bears, nonrepairable_d4j | nonrepairable_bears

    # restrict consideration to only bugs analyzed for repairability
    any_dependent_all = any_dependent_all & (repairable_all | nonrepairable_all)
    any_nondependent_all = any_nondependent_all & (repairable_all | nonrepairable_all)

    num_changed_lines_dependent_patches = [bug_to_num_changed_lines[bug]
                                           for bug in any_dependent_all]
    num_changed_lines_nondependent_patches = [bug_to_num_changed_lines[bug]
                                              for bug in any_nondependent_all]
    num_changed_lines = num_changed_lines_dependent_patches + num_changed_lines_nondependent_patches
    any_dependent_2line = {bug for bug in any_dependent_all if bug_to_num_changed_lines[bug] == 2}
    any_nondependent_2line = {bug for bug in any_nondependent_all if bug_to_num_changed_lines[bug] == 2}
    run_contingency_analysis(repairable_all, nonrepairable_all, any_dependent_2line, any_nondependent_2line,
    "Combined D4J|Bears: control|data dependency and repairability, 2 lines",
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    any_dependent_3line = {bug for bug in any_dependent_all if bug_to_num_changed_lines[bug] == 3}
    any_nondependent_3line = {bug for bug in any_nondependent_all if bug_to_num_changed_lines[bug] == 3}
    run_contingency_analysis(repairable_all, nonrepairable_all, any_dependent_3line, any_nondependent_3line,
    "Combined D4J|Bears: control|data dependency and repairability, 3 lines",
    'repairable', 'nonrepairable', 'dependent', 'nondependent')
    any_dependent_4line = {bug for bug in any_dependent_all if bug_to_num_changed_lines[bug] == 4}
    any_nondependent_4line = {bug for bug in any_nondependent_all if bug_to_num_changed_lines[bug] == 4}
    run_contingency_analysis(repairable_all, nonrepairable_all, any_dependent_4line, any_nondependent_4line,
    "Combined D4J|Bears: control|data dependency and repairability, 4 lines",
    'repairable', 'nonrepairable', 'dependent', 'nondependent')

def test_coverage_and_repairability(multi_chunk_bugs_d4j, multi_chunk_bugs_bears, coverage_partitions_d4j, coverage_partitions_bears, tool_to_repaired_bugs):
    disj_d4j, inbtw_d4j, same_d4j = [partition & set(multi_chunk_bugs_d4j) for partition in coverage_partitions_d4j]
    ndisj_d4j = inbtw_d4j | same_d4j
    nsame_d4j = disj_d4j | inbtw_d4j
    repairable_d4j, nonrepairable_d4j = partition_bugs_by_repairability(multi_chunk_bugs_d4j)
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
    repairable_bears, nonrepairable_bears = partition_bugs_by_repairability(multi_chunk_bugs_bears)
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

def test_neg_variant_and_same_coverage(bugs_d4j, bugs_bears,
                                       one_neg_var_d4j, one_neg_var_bears,
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

def get_table_1_stats():
    # override definition
    mlocation_mtest_bugs = multi_location_bugs & multi_test_bugs

    proj_to_bugs = partition_bugs_by_project(all_bugs)
    proj_to_multi_line_bugs = partition_bugs_by_project(multi_line_bugs)
    proj_to_multi_location_bugs = partition_bugs_by_project(multi_location_bugs)
    proj_to_multi_test_bugs = partition_bugs_by_project(multi_test_bugs)
    proj_to_mlocation_mtest_bugs = partition_bugs_by_project(mlocation_mtest_bugs)
    proj_to_two_six_location_bugs = partition_bugs_by_project(two_six_location_bugs)

    table = []
    table.append(['Proj', 'Total', 'MLoc', '%', 'MLocMTest', '%', '2-6Loc', '%', 'MLine', '%']) # header row

    for proj in proj_to_bugs.keys():
        total = len(proj_to_bugs.get(proj, []))

        mlocation = len(proj_to_multi_location_bugs.get(proj, []))
        p_mlocation = percent(mlocation, total)

        mlocationmtest = len(proj_to_mlocation_mtest_bugs.get(proj, []))
        p_mlocationmtest = percent(mlocationmtest, total)

        two_six_loc = len(proj_to_two_six_location_bugs.get(proj, []))
        p_two_six = percent(two_six_loc, total)

        mline = len(proj_to_multi_line_bugs.get(proj, []))
        p_mline = percent(mline, total)

        proj_row = [proj, total,
                    mlocation, p_mlocation,
                    mlocationmtest, p_mlocationmtest,
                    two_six_loc, p_two_six,
                    mline, p_mline,
                    #validation_exp, p_validation_exp]
                    ]

        latex_line = f'{mlocation} & {p_mlocation} & {mlocationmtest} & {p_mlocationmtest} ' \
                     f'& {two_six_loc} & {p_two_six} & {mline} & {p_mline}'
        latex_line = latex_line.replace('%', '\\%')

        table.append(proj_row)
        #table.append([proj, latex_line])


    table_proj_rank = {'Proj': 0, 'Chart': 1, 'Closure': 2, 'Lang': 3, 'Math': 4, 'Mockito': 5, 'Time': 6, 'Cli': 7,
                       'Codec': 8, 'Collections': 9, 'Compress': 10, 'Csv': 11, 'Gson': 12, 'Jacksoncore': 13, 'Jacksondatabind': 14,
                       'Jacksonxml': 15, 'Jsoup': 16, 'Jxpath': 17,
                       'FasterXML-jackson-databind': 18, 'INRIA-Spoon': 19, 'Spring-data-commons': 20,
                       'Traccar-traccar': 21, 'Other-Bears': 22}
    table.sort(key=lambda row: table_proj_rank[row[0]])

    from tabulate import tabulate
    print(tabulate(table))

    print('D4J MLoc', (x := sum(row[2] for row in table[1:-5])), percent(x, len(all_d4j_bugs)))
    print('D4J MLocMTest', (x := sum(row[4] for row in table[1:-5])), percent(x, len(all_d4j_bugs)))
    print('D4J 2-6Loc', (x := sum(row[6] for row in table[1:-5])), percent(x, len(all_d4j_bugs)))
    print('D4J MLine', (x := sum(row[8] for row in table[1:-5])), percent(x, len(all_d4j_bugs)))

    print('Bears MLoc', (x := sum(row[2] for row in table[-5:])), percent(x, len(all_bears_bugs)))
    print('Bears MLocMTest', (x := sum(row[4] for row in table[-5:])), percent(x, len(all_bears_bugs)))
    print('Bears 2-6Loc', (x := sum(row[6] for row in table[-5:])), percent(x, len(all_bears_bugs)))
    print('Bears MLine', (x := sum(row[8] for row in table[-5:])), percent(x, len(all_bears_bugs)))

def audit_clones_and_partial_repairs():
    clones_evaluated_bugs = set(get_bug_list('../Code Clones/all_evaluated_bugs.data'))
    partialrepair_evaluated_bugs = set(get_bug_list('../Partial Repairs/all_evaluated_bugs_unminimized.data'))

    print(sorted(partialrepair_evaluated_bugs - clones_evaluated_bugs))

if __name__=='__main__':
    audit_clones_and_partial_repairs()