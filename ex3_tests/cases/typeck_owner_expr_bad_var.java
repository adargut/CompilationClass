class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A {
	int fun(int[] b) {
		return (b).fun();
	}

}

class B {
	int fun() {
		return 3;
	}

}

