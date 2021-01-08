class Main {
	public static void main(String[] args) {
		while ((new Tester()).test()) {			{
			}

		}
	}
}

class Tester {
	boolean test() {
		int tmp;
		A a;
		a = new A();
		tmp = (a).f();
		a = new B();
		tmp = (a).f();
		a = new C();
		tmp = (a).f();
		a = new D();
		tmp = (a).f();
		return false;
	}

}

class A {
	int f() {
		int tmp;
		tmp = (this).g();
		tmp = (this).h();
		return 0;
	}

	int g() {
		System.out.println(0);
		return 0;
	}

	int h() {
		System.out.println(1);
		return 0;
	}

}

class B extends A {
	int g() {
		System.out.println(2);
		return 0;
	}

}

class C extends A {
	int h() {
		System.out.println(3);
		return 0;
	}

}

class D extends C {
	int f() {
		int tmp;
		tmp = (this).z();
		tmp = (this).g();
		tmp = (this).h();
		return 0;
	}

	int z() {
		System.out.println(4);
		return 0;
	}

}

