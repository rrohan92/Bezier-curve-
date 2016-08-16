import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;


public class CG_hw6
{

	int INSIDE = 0; // 0000
	int LEFT = 1;   // 0001
	int RIGHT = 2;  // 0010
	int BOTTOM = 4; // 0100
	int TOP = 8;    // 1000
	int world_x1 = 0, world_y1 = 0, world_x2 = 250, world_y2 = 250;
	int view_x1 = 0, view_y1 = 0, view_x2 = 200, view_y2 = 200;
	float scaling_factor = 1.0f;
	float increment = 0.05f;
	int width;
	int height;
	int rotation = 0, translation_x = 0, translation_y = 0;
	int pixels [][];
	String input = "ExtraCredit.ps";
	List<List<Integer>> all_curves = new ArrayList<List<Integer>>();
	List<List<List<Float>>> all_lines = new ArrayList<List<List<Float>>>();
	List<List<List<Float>>> transformed_lines = new ArrayList<List<List<Float>>>();
	List<List<Float>> clipped_lines = new ArrayList<List<Float>>();
	List<List<Float>> viewport_curves = new ArrayList<List<Float>>();
	List<List<Float>> only_lines = new ArrayList<List<Float>>();
	List<List<Float>> transformed_lines2 = new ArrayList<List<Float>>();


	public void curvestoLines()
	{
		for(int i=0; i<all_curves.size(); i++)
		{
			int x1 = all_curves.get(i).get(0);
			int y1 = all_curves.get(i).get(1);
			int x2 = all_curves.get(i).get(2);
			int y2 = all_curves.get(i).get(3);
			int x3 = all_curves.get(i).get(4);
			int y3 = all_curves.get(i).get(5);
			int x4 = all_curves.get(i).get(6);
			int y4 = all_curves.get(i).get(7);

			List<List<Float>> curve = new ArrayList<List<Float>>();
			int count = 0;

			for(float t=0f; t<=1f; t+=increment)
			{
				List<Float> line = new ArrayList<Float>();

				float xNew = (float) ((Math.pow(1-t, 3) * x1) + (3*t * Math.pow(1-t, 2) * x2)
						+ (3*t*t*(1-t)*x3) + (t*t*t*x4)); 

				float yNew = (float) ((Math.pow(1-t, 3) * y1) + (3*t * Math.pow(1-t, 2) * y2)
						+ (3*t*t*(1-t)*y3) + (t*t*t*y4));

				line.add(xNew);
				line.add(yNew);

				curve.add(line);

				if(t + increment > 1f && count==0)
				{
					t = 1.0f;
					List<Float> line1 = new ArrayList<Float>();

					float xNew1 = (float) ((Math.pow(1-t, 3) * x1) + (3*t * Math.pow(1-t, 2) * x2)
							+ (3*t*t*(1-t)*x3) + (t*t*t*x4)); 

					float yNew1 = (float) ((Math.pow(1-t, 3) * y1) + (3*t * Math.pow(1-t, 2) * y2)
							+ (3*t*t*(1-t)*y3) + (t*t*t*y4));

					line1.add(xNew1);
					line1.add(yNew1);

					curve.add(line1);	
					count++;
				}
				
			}

			all_lines.add(curve);
		}

	}

	public int updateBits(float x, float y)
	{
		int code = INSIDE;

		//Setting bits
		if (x < world_x1)
			code += LEFT;

		if(x > world_x2)
			code += RIGHT;

		if(y > world_y2)
			code += TOP;

		if(y < world_y1)
			code += BOTTOM;

		return code;

	}

	public void clipping()
	{
		for (int i=0; i<transformed_lines.size(); i++)
		{
			for(int j=0; j<transformed_lines.get(i).size()-1; j++)
			{
				float x1 = transformed_lines.get(i).get(j).get(0);
				float y1 = transformed_lines.get(i).get(j).get(1);
				float x2 = transformed_lines.get(i).get(j+1).get(0);
				float y2 = transformed_lines.get(i).get(j+1).get(1);

				int code1 = updateBits(x1, y1); 		
				int code2 = updateBits(x2, y2);

				boolean accept = false;


				while(true)
				{
					//Line is visible
					if((code1 | code2) == 0)
					{
						accept = true;
						break;
					}

					//Line is invisible
					else if((code1 & code2) != 0)
						break;

					//Line clipping
					else
					{
						float x = 0.0f,y = 0.0f;

						int codeout;

						if(code1 >= 1)
							codeout = code1;
						else 
							codeout = code2;

						//Line intersects top of window
						if((codeout & TOP) >= 1)
						{
							x = x1 + (x2 - x1) * (world_y2 - y1) / (y2 - y1);
							y = world_y2;
						}
						//Line intersects bottom of window
						else if((codeout & BOTTOM) >= 1)
						{
							x = x1 + (x2 - x1) * (world_y1 - y1) / (y2 - y1);
							y = world_y1;
						}
						//Line intersects right of window
						else if((codeout & RIGHT) >= 1)
						{
							y = y1 + (y2 - y1) * (world_x2 - x1) / (x2 - x1);
							x = world_x2;
						}
						//Line intersects left of window
						else if((codeout & LEFT) >= 1)
						{
							y = y1 + (y2 - y1) * (world_x1 - x1) / (x2 - x1);
							x = world_x1;
						}

						if(codeout == code1)
						{
							x1 = x;
							y1 = y;
							code1 = updateBits(x1, y1);
						}
						else
						{
							x2 = x;
							y2 = y;
							code2 = updateBits(x2, y2);
						}
					}
				}

				if(accept)
				{

					List<Float> row = new ArrayList<Float>();
					row.add(x1);
					row.add(y1);
					row.add(x2);
					row.add(y2);

					clipped_lines.add(row);
				}
			}
		}
	}
	
	public void clipping_lines()
	{
		for (int i=0; i<transformed_lines2.size(); i++)
		{
			for(int j=0; j<transformed_lines2.get(i).size(); j++)
			{
				float x1 = transformed_lines2.get(i).get(0);
				float y1 = transformed_lines2.get(i).get(1);
				float x2 = transformed_lines2.get(i).get(2);
				float y2 = transformed_lines2.get(i).get(3);

				int code1 = updateBits(x1, y1); 		
				int code2 = updateBits(x2, y2);

				boolean accept = false;


				while(true)
				{
					//Line is visible
					if((code1 | code2) == 0)
					{
						accept = true;
						break;
					}

					//Line is invisible
					else if((code1 & code2) != 0)
						break;

					//Line clipping
					else
					{
						float x = 0.0f,y = 0.0f;

						int codeout;

						if(code1 >= 1)
							codeout = code1;
						else 
							codeout = code2;

						//Line intersects top of window
						if((codeout & TOP) >= 1)
						{
							x = x1 + (x2 - x1) * (world_y2 - y1) / (y2 - y1);
							y = world_y2;
						}
						//Line intersects bottom of window
						else if((codeout & BOTTOM) >= 1)
						{
							x = x1 + (x2 - x1) * (world_y1 - y1) / (y2 - y1);
							y = world_y1;
						}
						//Line intersects right of window
						else if((codeout & RIGHT) >= 1)
						{
							y = y1 + (y2 - y1) * (world_x2 - x1) / (x2 - x1);
							x = world_x2;
						}
						//Line intersects left of window
						else if((codeout & LEFT) >= 1)
						{
							y = y1 + (y2 - y1) * (world_x1 - x1) / (x2 - x1);
							x = world_x1;
						}

						if(codeout == code1)
						{
							x1 = x;
							y1 = y;
							code1 = updateBits(x1, y1);
						}
						else
						{
							x2 = x;
							y2 = y;
							code2 = updateBits(x2, y2);
						}
					}
				}

				if(accept)
				{

					List<Float> row = new ArrayList<Float>();
					row.add(x1);
					row.add(y1);
					row.add(x2);
					row.add(y2);

					clipped_lines.add(row);
				}
			}
		}
	}
	
	public void viewport_transformation()
	{
		//Translation to origin of world window
		List<List<Float>> translated_polygons = new ArrayList<List<Float>>();

		for(int i=0; i<clipped_lines.size(); i++)
		{
			List<Float> line = new ArrayList<>();
			for(int j=0; j<4; j+=2)
			{

				float x = clipped_lines.get(i).get(j);
				float y = clipped_lines.get(i).get(j+1);

				x = x - world_x1;
				y = y - world_y1;

				line.add(x);
				line.add(y);
			}

			translated_polygons.add(line);
		}

		//Scaling to viewport
		List<List<Float>> scaled_polygons = new ArrayList<List<Float>>();

		for (int i=0; i<translated_polygons.size(); i++)
		{
			List<Float> line = new ArrayList<>();

			for(int j=0; j<4; j+=2)
			{
				float x = translated_polygons.get(i).get(j);
				float y = translated_polygons.get(i).get(j+1);

				float num_x = view_x2-view_x1;
				float num_y = view_y2-view_y1;
				float den_x = world_x2-world_x1;
				float den_y = world_y2-world_y1;

				x = x * (num_x/den_x);
				y = y * (num_y/den_y);

				line.add(x);
				line.add(y);
			}
			scaled_polygons.add(line);
		}

		//Translating to viewport origin
		for(int i=0; i<scaled_polygons.size(); i++)
		{
			List<Float> line = new ArrayList<>();

			for(int j=0; j<4; j+=2)
			{
				float x = scaled_polygons.get(i).get(j);
				float y = scaled_polygons.get(i).get(j+1);

				x = x + view_x1;
				y = y + view_y1;

				line.add(x);
				line.add(y);
			}

			viewport_curves.add(line);

		}
	}


	public void drawing()
	{
		for (int i=0; i<height; i++)
		{
			for (int j=0; j<width; j++)
			{
				pixels[i][j] = 0;
			}	
		}

		for (int i=0; i<viewport_curves.size(); i++)
		{
			float x1 = viewport_curves.get(i).get(0);
			float y1 = viewport_curves.get(i).get(1);
			float x2 = viewport_curves.get(i).get(2);
			float y2 = viewport_curves.get(i).get(3);


			//DDA
			float steps;
			float xc,yc;
			float x,y;

			float dx = x2 - x1;
			float dy = y2 - y1;

			if(Math.abs(dx) > Math.abs(dy))
				steps = Math.abs(dx);

			else
				steps = Math.abs(dy);

			if(x1 == x2 && dy < 0)
				steps =  Math.abs(dy);

			xc = dx/steps;

			yc = dy/steps;

			x = x1;

			y = y1;

			for (int s=0; s<steps; s++)
			{
				//if(!(x < view_x1 || y < view_y1 || x > view_x2 || y > view_y2))
				pixels[Math.round(y)][Math.round(x)] = 1;

				x = x + xc;
				y = y + yc;
			}
		}
	}

	public void output() throws FileNotFoundException, UnsupportedEncodingException
	{
		System.out.println("/*XPM*/");
		System.out.println("static char *sco100[] = { ");
		System.out.println("/* width height num_colors chars_per_pixel */ ");
		System.out.println("\""+ width + " " + height + " " + "2" + " " + "1" + "\"" + ",");
		System.out.println("/*colors*/");
		System.out.println("\""+ "0" + " " + "c" + " " + "#" + "ffffff" + "\"" + "," );
		System.out.println("\""+ "1" + " " + "c" + " " + "#" + "000000" + "\"" + "," );
		System.out.println("/*pixels*/");
		for (int i=0; i<height; i++)
		{
			System.out.print("\"");
			for(int j=0; j<width; j++)
			{
				System.out.print(pixels[height-i-1][j]);
			}
			if(i == height - 1)
				System.out.print("\"");
			else
				System.out.print("\"" + ",");

			System.out.println();
		}

		System.out.println("};");
	}

	public void transformation()
	{
		//Scaling for curves

		List<List<List<Float>>> scaled_lines = new ArrayList<List<List<Float>>>();

		for (int i=0; i<all_lines.size(); i++)
		{
			List<List<Float>> curve = new ArrayList<List<Float>>();

			for(int j=0; j<all_lines.get(i).size(); j++)
			{
				List<Float> row = new ArrayList<Float>();
				for(int k=0; k<2; k++)
				{
					float temp = all_lines.get(i).get(j).get(k);
					temp = temp * scaling_factor;

					row.add(temp);
				}
				curve.add(row);
			}
			scaled_lines.add(curve);
		}

		//Scaling for lines
		   List<List<Float>> scaled_lines2 = new ArrayList<List<Float>>();

	    	for (int i=0; i<only_lines.size(); i++)
	    	{
	    		List<Float> row = new ArrayList<Float>();

	    		for(int j=0; j<4; j++)
	    		{
	    			
	        		float temp = only_lines.get(i).get(j);
	        		temp = temp * scaling_factor;
	                row.add(temp);
	    		}
	    		scaled_lines2.add(row);
	    	}
		//Rotation
		List<List<List<Float>>> rotated_lines = new ArrayList<List<List<Float>>>();

		for(int i=0; i<scaled_lines.size(); i++)
		{
			List<List<Float>> curve = new ArrayList<List<Float>>();

			for(int j=0; j<scaled_lines.get(i).size(); j++)
			{
				List<Float> row1 = new ArrayList<Float>();
				float x = scaled_lines.get(i).get(j).get(0);
				float y = scaled_lines.get(i).get(j).get(1);

				double x_prime = x * Math.cos(Math.toRadians(rotation)) - y * Math.sin(Math.toRadians(rotation));
				double y_prime = x * Math.sin(Math.toRadians(rotation)) + y * Math.cos(Math.toRadians(rotation));

				row1.add((float)x_prime);
				row1.add((float)y_prime);
				curve.add(row1);
			}
			rotated_lines.add(curve);
		}

		//Rotation for lines
        List<List<Float>> rotated_lines2 = new ArrayList<List<Float>>();
        
		for(int i=0; i<scaled_lines2.size(); i++)
        {
			List<Float> row1 = new ArrayList<Float>();

        	for(int j=0; j<4; j+=2)
        	{
        		float x = scaled_lines2.get(i).get(j);
        		float y = scaled_lines2.get(i).get(j+1);
        		double x_prime = x * Math.cos(Math.toRadians(rotation)) - y * Math.sin(Math.toRadians(rotation));
        		double y_prime = x * Math.sin(Math.toRadians(rotation)) + y * Math.cos(Math.toRadians(rotation));

        		row1.add((float)x_prime);
        		row1.add((float)y_prime);
        	}
        	rotated_lines2.add(row1);
        }
		
		//Translation
		for(int i=0; i<rotated_lines.size(); i++)
		{
			List<List<Float>> curve = new ArrayList<List<Float>>();

			for(int j=0; j<rotated_lines.get(i).size(); j++)
			{
				List<Float> row2 = new ArrayList<Float>();
				float x = rotated_lines.get(i).get(j).get(0);
				float y = rotated_lines.get(i).get(j).get(1);

				x = x + translation_x;
				y = y + translation_y;

				row2.add(x);
				row2.add(y);
				curve.add(row2);
			}
			transformed_lines.add(curve);
		}
		
		  //Translation for lines
        for(int i=0; i<rotated_lines2.size(); i++)
        {
    		List<Float> row2 = new ArrayList<Float>();

        	for(int j=0; j<4; j+=2)
        	{
        		float x = rotated_lines2.get(i).get(j);
        		float y = rotated_lines2.get(i).get(j+1);
        		x = x + translation_x;
        		y = y + translation_y;
        		row2.add(x);
        		row2.add(y);
        	}
        	
        	transformed_lines2.add(row2);
        }
	}

	public void read_file(String input) throws FileNotFoundException
	{
		File file = new File(input);
		Scanner sc = new Scanner(file);
		while(sc.hasNextLine())
		{
			if(sc.nextLine().equals("%%%BEGIN"))
			{
				List<Integer> curve = new ArrayList<Integer>();
				String line = sc.nextLine();

				while(sc.hasNextLine())
				{
					line = sc.nextLine();

					if(line.equals("%%%END"))
						break;

					if(line.trim().length() == 0)               //For blank lines
						continue;

					if(line.equals("stroke"))                     //One curve done
					{
						all_curves.add(curve);
						curve = new ArrayList<Integer>();
						continue;
					}

					String parse[] = line.split(" ");


					if(parse[2].equals("moveto"))
					{
						int x = Integer.parseInt(parse[0]);
						int y = Integer.parseInt(parse[1]);

						curve.add(x);
						curve.add(y);

						continue;
					}

					else if(parse[4].equals("Line"))
					{
						List<Float> lineRow = new ArrayList<Float>();
						int x1 = Integer.parseInt(parse[0]);
						int y1 = Integer.parseInt(parse[1]);
						int x2 = Integer.parseInt(parse[2]);
						int y2 = Integer.parseInt(parse[3]);

						lineRow.add((float) x1);
						lineRow.add((float) y1);
						lineRow.add((float) x2);
						lineRow.add((float) y2);

						only_lines.add(lineRow);
						 
						continue;
					}

					else if(parse[6].equals("curveto"))
					{
						int x1 = Integer.parseInt(parse[0]);
						int y1 = Integer.parseInt(parse[1]);
						int x2 = Integer.parseInt(parse[2]);
						int y2 = Integer.parseInt(parse[3]);
						int x3 = Integer.parseInt(parse[4]);
						int y3 = Integer.parseInt(parse[5]);

						curve.add(x1);
						curve.add(y1);
						curve.add(x2);
						curve.add(y2);
						curve.add(x3);
						curve.add(y3);

						continue;
					}		
				}
			}				
		}

		sc.close();   
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		CG_hw6 obj = new CG_hw6();

		for (int i=0; i<args.length; i+=2)
		{
			if(args[i].equals("-f"))
				obj.input = args[i+1];

			if(args[i].equals("-a"))
				obj.world_x1 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-b"))
				obj.world_y1 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-c"))
				obj.world_x2 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-d"))
				obj.world_y2 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-r"))
				obj.rotation = Integer.parseInt(args[i+1]);

			if(args[i].equals("-m"))
				obj.translation_x = Integer.parseInt(args[i+1]);

			if(args[i].equals("-n"))
				obj.translation_y = Integer.parseInt(args[i+1]);

			if(args[i].equals("-s"))
				obj.scaling_factor = Float.parseFloat(args[i+1]);

			if(args[i].equals("-j"))
				obj.view_x1 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-k"))
				obj.view_y1 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-o"))
				obj.view_x2 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-p"))
				obj.view_y2 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-L"))
				obj.increment = Float.parseFloat(args[i+1]);
		}
		
		obj.read_file(obj.input);
		obj.width = 501;
		obj.height = 501;
		obj.pixels = new int[obj.height][obj.width];
		obj.curvestoLines();
		obj.transformation();
		obj.clipping();
		obj.clipping_lines();
		obj.viewport_transformation();
		obj.drawing();
		obj.output();
	}
}
