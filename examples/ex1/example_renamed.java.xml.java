class Main {
	public static void main(String[] args) {
		System.out.println((new Trivial2()).Start(1, 2));
	}
}

class Trivial {
	int f;

	int Start2(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (4);
		return 0;
	}

}

class Simple {
	int flee(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (3);
		return 0;
	}

}

class Trivial2 extends Trivial {
	int f;

	int Start(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (4);
		return 0;
	}

}

class Trivial3 extends Trivial {
	int x;

	int Start(int a, int b) {
		Simple x;
		int y;
		x = a;
		y = (b) + (4);
		return (x).flee();
	}

	int Start3(int a, int b) {
		Trivial2 x;
		int y;
		x = a;
		y = (b) + (4);
		return (x).Start();
	}

}

class Trivial4 extends Trivial2 {
	int f;

	int Start(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (4);
		return 0;
	}

	int Start2(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (4);
		return 0;
	}

}

