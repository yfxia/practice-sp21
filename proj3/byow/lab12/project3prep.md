# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer:
One major part differs is the use of recursion to create hexagons versus using iteration to create it.
-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer:
A tesselation of hexagon is to create a groups of hexagons tiles composed of a single hexagon. It has 19 hexagons in total and arranged in 5 vertical columns with counts: 3,4,5,4,3.
-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer:
I would start writing helper method of how to form a single shape object in the world. Then having other methods to form a world built by these shape objects.

-----
**What distinguishes a hallway from a room? How are they similar?**

Answer:
Hallway a 1-D on the grid, but room is 2-D width forming an area one can walk in.
