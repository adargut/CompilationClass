class Main {
	public static void main(String[] args) {
		System.out.println(1);
	}
}

class A {
}

class B extends A {
	int theMethod() {
		return 1;
	}

}

class C extends A {
	int theMethod() {
		return 1;
	}

}

class D extends C {
	int anotherMethod(B b) {
		int max;
		while (!(((b).theMethod()) && ((this).theMethod()))) {			{
				max = ((this).theMethod()) + ((b).theMethod());

			}

		}
		return max;
	}

}

