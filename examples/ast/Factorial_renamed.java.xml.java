class Factorial {
	public static void main(String[] a) {
		System.out.println((new Fac()).ComputeFac(10));
	}
}

class Fac {
	int ComputeFac(int test) {
		int num_aux;
		if ((test) < (1))
			{
				num_aux = 1;

			}
		else
			{
				num_aux = (test) * ((this).ComputeFac((test) - (1)));

			}
		return num_aux;
	}

}

