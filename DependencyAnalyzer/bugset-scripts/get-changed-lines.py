import argparse
import subprocess
import re

if __name__=='__main__':
    #define inpput
    parser = argparse.ArgumentParser()
    parser.add_argument('buggy_src', help='source code directory of buggy program')
    parser.add_argument('fixed_src', help='source code directory of fixed program')

    #parse input
    args = parser.parse_args()
    dir_src_buggy = args.buggy_src
    dir_src_fixed = args.fixed_src

    find_changed_files_cmd = ['diff', '-rqbB', dir_src_buggy, dir_src_fixed]
    changed_files_rawout = subprocess.run(find_changed_files_cmd, capture_output=True).stdout.decode('utf-8').strip()
    rawout_line_regex=re.compile(r'Files (.*) and (.*) differ')

    changed_files = list() #list of tuples (buggy file path, fixed file path)
    for rawout_line in changed_files_rawout.splitlines():
        chg_file_b, chg_file_f = rawout_line_regex.search(rawout_line).groups()
        changed_files.append((chg_file_b, chg_file_f))

