#!/bin/bash

dir="FastXML_PfastreXML"
dataset=$1
data_dir="data/$dataset"
results_dir="results/$dataset"
model_dir="results/$dataset/model"

mkdir results/ ;
mkdir $results_dir ;
mkdir $model_dir ;

trn_ft_file="${data_dir}/trn_X_Xf.txt"
trn_lbl_file="${data_dir}/trn_X_Y.txt"
tst_ft_file="${data_dir}/tst_X_Xf.txt"
tst_lbl_file="${data_dir}/tst_X_Y.txt"
inv_prop_file="${data_dir}/inv_prop.txt"
score_file="${results_dir}/score_mat.txt"

perl $dir/Tools/convert_format.pl $data_dir/train.txt $trn_ft_file $trn_lbl_file
perl $dir/Tools/convert_format.pl $data_dir/test.txt $tst_ft_file $tst_lbl_file

# training
# Reads training features (in $trn_ft_file), training labels (in $trn_lbl_file), and writes FastXML model to $model_dir
./$dir/FastXML/fastXML_train $trn_ft_file $trn_lbl_file $model_dir #-T 5 -s 0 -t 5 -b 1.0 -c 1.0 -m 10 -l 10

# testing
# Reads test features (in $tst_ft_file), FastXML model (in $model_dir), and writes test label scores to $score_file
./$dir/FastXML/fastXML_test $tst_ft_file $score_file $model_dir

# performance evaluation 
#matlab -nodesktop -nodisplay -r "cd('$PWD'); addpath(genpath('$dir/Tools')); trn_X_Y = read_text_mat('$trn_lbl_file'); tst_X_Y = read_text_mat('$tst_lbl_file'); wts = inv_propensity(trn_X_Y,0.55,1.5); score_mat = read_text_mat('$score_file'); get_all_metrics(score_mat, tst_X_Y, wts); exit;"

