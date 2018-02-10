function P = precision_k(score_mat,true_mat,K)
	P = helper(score_mat,true_mat,K);
end

function P = helper(score_mat,true_mat,K)
	num_inst = size(score_mat,2);
	num_lbl = size(score_mat,1);

	P = zeros(K,1);
	rank_mat = sort_sparse_mat(score_mat);

	for k=1:K
		mat = rank_mat;
%The 4 lines below are used because some sparse matrices are not converted correctly with mat(rank_mat>k) = 0;
		[i,j,v] = find(mat);
		[m,n] = size(mat);
		v(v>k) = 0;
		mat = sparse(i,j,v,m,n);
	%	mat(rank_mat>k) = 0;
%		disp(m);
%		disp(n);
%		disp(size(true_mat));
		mat = spones(mat);
		mat = mat.*true_mat;
		num = sum(mat,1);

		P(k) = mean(num/k);
	end
end
