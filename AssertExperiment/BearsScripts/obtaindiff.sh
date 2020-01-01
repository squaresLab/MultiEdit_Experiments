#!/bin/bash



mkdir $3/"$1""$2"/
mkdir $3/"$1""$2"/bugorig/
mkdir $3/"$1""$2"/fix/

python $BEARSPATH/scripts/checkout_bug.py --bugId Bears-$2 --workspace $3/"$1""$2"/bugorig
cp checkout_fix.py  $BEARSPATH/scripts/checkout_fix.py 
python $BEARSPATH/scripts/checkout_fix.py --bugId Bears-$2 --workspace $3/"$1""$2"/fix
diff -r $3/"$1""$2"/bugorig/Bears-$2/$4 $3/"$1""$2"/fix/Bears-$2/$4 > $3/"$1""$2"/diff.diff

