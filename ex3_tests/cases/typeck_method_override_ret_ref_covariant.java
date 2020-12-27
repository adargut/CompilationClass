class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A {
	C fun(int x) {
		return new C();
	}

}

class B extends A {
	D fun(int x) {
		return new D();
	}

}

class C {
}

class D extends C {
}

