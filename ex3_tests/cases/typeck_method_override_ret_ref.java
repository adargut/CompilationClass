class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class A {
	A fun(int x) {
		return this;
	}

}

class B extends A {
	A fun(int x) {
		return this;
	}

}

