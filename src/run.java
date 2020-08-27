import network.graph;

public class run
{
	public static void main(String[] args)
	{
		graph network = new graph();
		network.buildGraphFromFile("./files/networks/testnetwork3.txt");
		System.out.println(network.toString());
	}
}
