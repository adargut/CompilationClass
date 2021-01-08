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
	E fun(int x) {
		return new E();
	}

}

class C extends B {
	F fun(int z) {
		return new F();
	}

}

class D {
}

class E extends D {
}

class F extends E {
}

