class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A {
	int fun() {
		return (this).fun2(this);
	}

	int fun2(int x) {
		return x;
	}

}

