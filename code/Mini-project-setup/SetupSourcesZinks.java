import java.util.*;
import java.lang.*;

class SetupSourcesSinks  {

private int sources ;
private int sinks ;
Random rand = new Random(2);

public SetupSourcesSinks(int sources, int sinks) {
  this.sources = sources;
  this.sinks = sinks;
}

private void generate() {

  while(sources || sinks != 0) {
      if(rand.nextInt() == 0) {
        new Source();
        this.sources--;}
      else{
        new sink();
        this.sinks--;}
  }

  if(sources != 0) {
    for(int i = sources; i>0; i--) {
      new Source();
      }
    }
else if(sinks != 0) {
  for(int i = sinks; i>0; i--) {
    new sink();
    }
  }
}


public static void main (String args[]){

  new Server();
  try {
  new SetupSourcesSinks(args[0], args[1]).generate();}
  catch (Exception e) { System.out.println("Only 2 Integer inputs are allowed"); }
  }
}
