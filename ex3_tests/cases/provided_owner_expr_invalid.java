class Main {
	public static void main(String[] a) {
		System.out.println(3);
	}
}

class Tree {
	Tree left;

	Tree right;

	Tree getLeft() {
		return left;
	}

	Tree getRight() {
		return right;
	}

	int num() {
		return 2;
	}

	int fun() {
		Tree t;
		return ((new Tree()).getLeft()).fun();
	}

}

