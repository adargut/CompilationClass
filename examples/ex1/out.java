class Main {
	public static void main(String[] args) {
		System.out.println((new Example()).run());
	}
}

class Example {
	int run() {
		Example e;
		e = new Example();
		return (e).run();
	}

}

class NonExample {
	int run() {
		return (new NonExample()).run();
	}

}

