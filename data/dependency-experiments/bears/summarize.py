import glob
import csv
import re

badbugs = set()

def hasDep(bugdir, type):
    global badbugs
    outputfiles = glob.glob('{}dep-analysis-output/*.{}'.format(bugdir, type))
    logfiles = glob.glob('{}dep-analysis-output/*.{}.log'.format(bugdir, type))

    if len(outputfiles) < len(logfiles):
        #something went wrong during analysis
        badbugs.add(bugdir)

    for outfilepath in outputfiles:
        with open(outfilepath) as outfile:
            firstline = outfile.readline().strip()
            if firstline == 'true':
                #report true if even one analysis result reported true
                return True
            elif firstline == 'false':
                continue
            else:
                badbugs.add(firstline)

    #default case if there are no outputfiles or if no output file reported true as the analysis result
    return False

def update(bugname, bughasDc, bughasDf, bughasDa, bughasDo, results):
    if bugname not in results:
        results[bugname] = [False, False, False, False, False, False]

    results[bugname][0] |= bughasDc
    results[bugname][1] |= bughasDf
    results[bugname][2] |= bughasDa
    results[bugname][3] |= bughasDo
    bug_has_data_dep = bughasDf or bughasDa or bughasDo
    results[bugname][4] |= bug_has_data_dep
    bug_has_any_dep = bughasDc or bug_has_data_dep
    results[bugname][5] |= bug_has_any_dep

def natural_order_key(bug):
    #Extract the number from "Bears-n"
    return int(bug[6:])

def write_to_csv(results, csvfilename):
    with open(csvfilename, 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(['Bug', 'hasControlDependency', 'hasFlowDataDependency',
                            'hasAntiDataDependency', 'hasOutDataDependency',
                            'hasAnyDataDependency', 'hasAnyDependency'])
        bugs_ordered = list(results.keys())
        bugs_ordered.sort(key=natural_order_key)
        for bug in bugs_ordered:
            bugrow = [bug] + results[bug]
            csvwriter.writerow(bugrow)

if __name__ == '__main__':
    results = dict() # Map: BugNameString -> 6-Tuple (Dc, Df, Da, Do, has-any-data-dependency, has-any-dependency)

    bugdirs_buggy = glob.glob('Buggy/*/')
    bugdirs_fixed = glob.glob('Fixed/*/')
    bugdirs = bugdirs_buggy + bugdirs_fixed
    #bugdirs.remove('logs/')

    for bugdir in bugdirs:
        bugname = bugdir[6:-1] #truncate the 'Buggy/' or 'Fixed/' and the ending '/'
        bughasDc = hasDep(bugdir, 'Dc')
        bughasDf = hasDep(bugdir, 'Df')
        bughasDa = hasDep(bugdir, 'Da')
        bughasDo = hasDep(bugdir, 'Do')

        update(bugname, bughasDc, bughasDf, bughasDa, bughasDo, results)

    print("Detected {} problematic analyses".format(len(badbugs)))

    write_to_csv(results, csvfilename='results.csv')
