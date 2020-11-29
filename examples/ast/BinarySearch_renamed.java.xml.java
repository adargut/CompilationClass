class BinarySearch {
	public static void main(String[] a) {
		System.out.println((new BS()).Start(20));
	}
}

class BS {
	int[] test;

	int size;

	int Start(int sz) {
		int aux01;
		int aux02;
		aux01 = (this).Init(sz);
		aux02 = (this).Print();
		if ((this).Search(8))
			System.out.println(1);
		else
			System.out.println(0);
		if ((this).Search(19))
			System.out.println(1);
		else
			System.out.println(0);
		if ((this).Search(20))
			System.out.println(1);
		else
			System.out.println(0);
		if ((this).Search(21))
			System.out.println(1);
		else
			System.out.println(0);
		if ((this).Search(37))
			System.out.println(1);
		else
			System.out.println(0);
		if ((this).Search(38))
			System.out.println(1);
		else
			System.out.println(0);
		if ((this).Search(39))
			System.out.println(1);
		else
			System.out.println(0);
		if ((this).Search(50))
			System.out.println(1);
		else
			System.out.println(0);
		return 999;
	}

	boolean Search(int num) {
		boolean bs01;
		int right;
		int left;
		boolean var_cont;
		int medium;
		int aux01;
		int nt;
		aux01 = 0;
		bs01 = false;
		right = (test).length;
		right = (right) - (1);
		left = 0;
		var_cont = true;
		while (var_cont) {			{
				medium = (left) + (right);

				medium = (this).Div(medium);

				aux01 = (test)[medium];

				if ((num) < (aux01))
					right = (medium) - (1);
				else
					left = (medium) + (1);

				if ((this).Compare(aux01, num))
					var_cont = false;
				else
					var_cont = true;

				if ((right) < (left))
					var_cont = false;
				else
					nt = 0;

			}

		}
		if ((this).Compare(aux01, num))
			bs01 = true;
		else
			bs01 = false;
		return bs01;
	}

	int Div(int num) {
		int count01;
		int count02;
		int aux03;
		count01 = 0;
		count02 = 0;
		aux03 = (num) - (1);
		while ((count02) < (aux03)) {			{
				count01 = (count01) + (1);

				count02 = (count02) + (2);

			}

		}
		return count01;
	}

	boolean Compare(int num1, int num2) {
		boolean retval;
		int aux02;
		retval = false;
		aux02 = (num2) + (1);
		if ((num1) < (num2))
			retval = false;
		else
			if (!((num1) < (aux02)))
				retval = false;
			else
				retval = true;
		return retval;
	}

	int Print() {
		int j;
		j = 1;
		while ((j) < (size)) {			{
				System.out.println((test)[j]);

				j = (j) + (1);

			}

		}
		System.out.println(99999);
		return 0;
	}

	int Init(int sz) {
		int j;
		int k;
		int aux02;
		int aux01;
		size = sz;
		test = new int[sz];
		j = 1;
		k = (size) + (1);
		while ((j) < (size)) {			{
				aux01 = (2) * (j);

				aux02 = (k) - (3);

				test[j] = (aux01) + (aux02);

				j = (j) + (1);

				k = (k) - (1);

			}

		}
		return 0;
	}

}
