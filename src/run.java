import network.graph;

public class run
{
	public static void main(String[] args)
	{
		graph network = new graph();
		network.buildGraphFromFile("./files/networks/Euemailcomm_35-core.txt");
		System.out.println(network.toString());
	}
}
