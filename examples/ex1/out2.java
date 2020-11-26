//class Main {
//	public static void main(String[] args) {
//		System.out.println((new Example()).flee());
//	}
//}
//
//class Example {
//	int flee() {
//		Example e;
//		e = new NonExample();
//		return (e).flee();
//	}
//
//}
//
//class NonExample {
//	int run() {
//		return (new NonExample()).run();
//	}
//
//}
//
//class A {
//	int foo() {
//
//	}
//}
//
//class B extends A {
//	int foo() {
//
//	}
//}
//
//class C extends  B {
//	int foo() {
//		A a;
//		a = new C();
//		a.foo();
//
//		if True {
//			B a;
//		}
//	}
//}