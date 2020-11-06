class Main {
	public static void main(String[] args) {
		System.out.println((new Trivial()).flee(1, 2));
	}
}

class Trivial {
	int f;

	int flee(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (4);
		return 0;
	}

}

class Simple extends Trivial {
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

	int flee(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (4);
		return 0;
	}

}

class Trivial3 extends Trivial {
	int f;

	int flee(int a, int b) {
		Simple x;
		int y;
		x = a;
		y = (b) + (4);
		return (x).Start();
	}

}

class Trivial4 extends Trivial2 {
	int f;

	int flee(int a, int b) {
		int x;
		int y;
		x = a;
		y = (b) + (4);
		return 0;
	}

}

