#          mmrs
### SHORT DESCRIPTION
mmrs is memory management & process loading real-time simulator.
It simulates 3 memory architectures:  
* fixed -- memory is divided into few sections, and process may be loaded into the one which size is >= size of process.  
  2 algorythms to get section:  
  * find first which size is enough big;  
  * find the smallest which size is enough big.
    
  Advantages:  
  * simplicity;  
  * no external fragmentation.
    
  Disadvantages:  
  * limited process size;  
  * limited process count;  
  * internal fragmentation (in cases when section size is bigger than process address space).  
  MS DOS used something similar.  
* dynamic -- memory isn't divided into sections, and process gives what he need while loading.
  
  Advantages:  
  * no limits on process size & count;  
  * no internal fragmentation.
    
  Disadvantages:  
  * external fragmentation (in cases when after small process deletion hole appears, and bigger process cann't be loaded);  
  * periodical defragmentation is needed.  
  2 ways to determine when to start defragmentation:  
  * after each process addr space deallocation (inefficient);  
  * after fragmentation takes given percent, here is 2 ways too:  
     -- hole sizes together take given percent from full memory;  
     -- hole size count take given percent from total section count (section is hole or loaded process).  
* paged -- memory is divided to fixed count of fixed (relatively small) pages, process is loaded into generally non-continuous set of them.
  
  Advantages:  
  * no limits on process size & count;  
  * no external fragmentation.
    
  Disadvantages:  
  * internal fragmentation (in cases when process size >> page size ~ half of page).
  
In conclusion -- paged sistem must be the most flexible & efficient from this list.  
  
### SOFTWARE REQUIREMENTS  
Following programs are needed:  
* javac -- compiler java -> bytecode;  
* jar -- utility to create archive, which contains manifest (which holds meta-info like entry point & versions) & bytecode classes;  
* java -- bytecode interpreter.  

### KNOWN BUGS
One table column is drawn incorrectly after exchanging columns.

### BUILD & SET UP  
$ git clone https://github.com/sng7ca/mmrs.git # fetch sources from repository  
$ cd mmrs  
$ make  # builds mmrs.jar, which is archive with bytecode for JVM  
$ make java # launch  
  
For testing you can use files from config/ with predefined configuration:  
![config](img/img0.png?raw=true "config")
  
![work](img/img1.png?raw=true "work")  
  
~~~~~~~~~~~~~~~
Stanislav Hubin
