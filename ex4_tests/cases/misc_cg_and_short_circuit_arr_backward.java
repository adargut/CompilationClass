class Main {
	public static void main(String[] args) {
		System.out.println((new Checker()).check());
	}
}

class Checker {
	public int check() {
		int[] arr;
		int i;
		arr = new int[5];
		i = 20;
		if (((5) < ((arr)[i])) && ((i) < ((arr).length)))
			System.out.println(0);
		else
			System.out.println(1);
		return 0;
	}

}

