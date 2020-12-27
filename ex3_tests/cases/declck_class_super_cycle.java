class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A extends C {
	int fun() {
		return 1;
	}

}

class B extends A {
	int fun() {
		return 0;
	}

}

class C extends B {
	int fun() {
		return 6;
	}

}

