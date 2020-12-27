class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A {
	int fun() {
		return (this).fun2(new C());
	}

	int fun2(A x) {
		return 5;
	}

}

class B {
}

class C extends B {
}

class D extends C {
}

