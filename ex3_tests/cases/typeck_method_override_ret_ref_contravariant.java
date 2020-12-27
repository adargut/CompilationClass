class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A {
	D fun(int x) {
		return new D();
	}

}

class B extends A {
	C fun(int x) {
		return new C();
	}

}

class C {
}

class D extends C {
}

