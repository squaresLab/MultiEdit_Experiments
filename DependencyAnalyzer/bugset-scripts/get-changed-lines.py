# Intended for both D4J and Bears

# usage: get-changed-lines.py [-h] buggy fixed src
#
# positional arguments:
# buggy       working directory of buggy program
# fixed       working directory of fixed program
# src         source code directory relative to working directories of buggy & fixed programs
#
# optional arguments:
# -h, --help  show this help message and exit

# output format (to stdout):
# output is grouped into 3-line chunks
# 1st line: a modified Java class (notated.with.dot.Notation)
# 2nd line: "Buggy: " followed by a space-separated list of line numbers present in Buggy but not present in Fixed
# 3rd line: "Fixed: " followed by a space-separated list of line numbers present in Fixed but not present in Buggy
#
# Example output:
# org.apache.commons.math3.fraction.BigFraction
# Buggy:
# Fixed: 306 307 308
# org.apache.commons.math3.fraction.Fraction
# Buggy:
# Fixed: 215 216 217

import argparse
import subprocess
import re
import os

def get_java_class(java_file_fullpath, dir_src_fullpath):
    assert java_file_fullpath[-5:] == '.java'
    java_file_relpath = os.path.relpath(java_file_fullpath, dir_src_fullpath)
    java_class = java_file_relpath[:-5].replace(os.sep, '.')
    return java_class


def get_changed_classes(dir_src_buggy, dir_src_fixed, dir_src_relative):
    #find which files are changed
    changed_classes = list() #list of tuples (java.class.name, buggy file path, fixed file path)

    find_changed_files_cmd = ['diff', '-rqbB', dir_src_buggy, dir_src_fixed]
    changed_files_rawout = subprocess.run(find_changed_files_cmd, stdout=subprocess.PIPE).stdout.decode('utf-8').strip()

    capture_paths_from_rawout_regex=re.compile(r'^Files (.*) and (.*) differ$')

    for rawout_line in changed_files_rawout.splitlines():
        chg_file_b, chg_file_f = capture_paths_from_rawout_regex.search(rawout_line).groups()
        java_class_b = get_java_class(chg_file_b, dir_src_buggy)
        java_class_f = get_java_class(chg_file_f, dir_src_fixed)
        assert java_class_b == java_class_f
        changed_classes.append((java_class_b, chg_file_b, chg_file_f))

    return changed_classes

def get_changed_lines(java_file_path_buggy, java_file_path_fixed):
    changed_lines_buggy, changed_lines_fixed = list(), list()

    #run command through the shell, otherwise double quotes will go crazy
    find_changed_lines_cmd = 'diff --unchanged-line-format="" --old-line-format=";%dn;%L" --new-line-format=":%dn:%L" ' \
                             '-bB {} {}'.format(java_file_path_buggy, java_file_path_fixed)
    changed_lines_rawout = subprocess.run(find_changed_lines_cmd, shell=True, stdout=subprocess.PIPE).stdout.decode('utf-8').strip()

    capture_linenum_buggy = re.compile(r'^;([0-9]+);')
    capture_linenum_fixed = re.compile(r'^:([0-9]+):')

    for rawout_line in changed_lines_rawout.splitlines():
        if len(rawout_line) == 0:
            continue
        if rawout_line[0] == ';': #old (buggy) line
            linenum = capture_linenum_buggy.search(rawout_line).group() #linenum is a string
            linenum = linenum[1:-1] #for some reason, the (semi)colons stay with the numbers
            changed_lines_buggy.append(linenum)
        elif rawout_line[0] == ':': #new (buggy) line
            linenum = capture_linenum_fixed.search(rawout_line).group()
            linenum = linenum[1:-1]
            changed_lines_fixed.append(linenum)

    return changed_lines_buggy, changed_lines_fixed

def get_changed_lines_mapping(changed_class_tuples):
    changed_lines_mapping = dict() #maps java class name -> (changed lines buggy, changed lines fixed)
    for changed_class_tuple in changed_class_tuples:
        java_class_name = changed_class_tuple[0]
        java_file_path_buggy = changed_class_tuple[1]
        java_file_path_fixed = changed_class_tuple[2]
        changed_lines_tuple = get_changed_lines(java_file_path_buggy, java_file_path_fixed)
        changed_lines_mapping[java_class_name] = changed_lines_tuple

    return changed_lines_mapping

def get_output_string(java_class, changed_lines_buggy, changed_lines_fixed):
    output = ''
    output += java_class + '\n'
    output += 'Buggy: '
    for line in changed_lines_buggy:
        output += line + ' '
    output += '\n'
    output += 'Fixed: '
    for line in changed_lines_fixed:
        output += line + ' '
    output += '\n'
    return output

if __name__=='__main__':
    #define inpput
    parser = argparse.ArgumentParser()
    parser.add_argument('buggy', help='working directory of buggy program')
    parser.add_argument('fixed', help='working directory of fixed program')
    parser.add_argument('src',   help='source code directory relative to working directories of buggy & fixed programs')

    #parse input
    args = parser.parse_args()
    wd_buggy = args.buggy
    wd_fixed = args.fixed
    dir_src_relative = args.src

    #convert working directory paths to absolute paths
    wd_buggy = os.path.abspath(wd_buggy)
    wd_fixed = os.path.abspath(wd_fixed)

    dir_src_buggy = wd_buggy + os.sep + dir_src_relative
    dir_src_fixed = wd_fixed + os.sep + dir_src_relative

    changed_classes = get_changed_classes(dir_src_buggy, dir_src_fixed, dir_src_relative)

    java_class_to_changed_lines_map = get_changed_lines_mapping(changed_classes)

    for java_class, changed_lines_tuple in java_class_to_changed_lines_map.items():
        print(get_output_string(java_class, *changed_lines_tuple), end='')