# usage: get-changed-lines.py [-h] buggy fixed src
#
# positional arguments:
# buggy       working directory of buggy program
# fixed       working directory of fixed program
# src         source code directory relative to working directories of buggy & fixed programs
#
# optional arguments:
# -h, --help  show this help message and exit

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
    changed_files_rawout = subprocess.run(find_changed_files_cmd, capture_output=True).stdout.decode('utf-8').strip()

    capture_paths_from_rawout_regex=re.compile(r'Files (.*) and (.*) differ')

    for rawout_line in changed_files_rawout.splitlines():
        chg_file_b, chg_file_f = capture_paths_from_rawout_regex.search(rawout_line).groups()
        java_class_b = get_java_class(chg_file_b, dir_src_buggy)
        java_class_f = get_java_class(chg_file_f, dir_src_fixed)
        assert java_class_b == java_class_f
        changed_classes.append((java_class_b, chg_file_b, chg_file_f)) #todo parse java classes

    return changed_classes

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

    print(dir_src_buggy, dir_src_fixed)

    changed_classes = get_changed_classes(dir_src_buggy, dir_src_fixed, dir_src_relative)
    print(changed_classes)
    #find what line got changed
    changed_lines = dict()