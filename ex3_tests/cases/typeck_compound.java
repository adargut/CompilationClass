class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A {
	int fun(int a, int b) {
		int[] arr;
		arr = (this).makeArr((a) + (5));
		arr[b] = 7;
		return (arr)[a];
	}

	int[] makeArr(int x) {
		return new int[x];
	}

}

