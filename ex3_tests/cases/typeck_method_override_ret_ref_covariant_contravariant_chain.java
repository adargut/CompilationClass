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
	F fun(int x) {
		return new F();
	}

}

class C extends B {
	E fun(int z) {
		return new E();
	}

}

class D {
}

class E extends D {
}

class F extends E {
}

