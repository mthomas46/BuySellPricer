Since I originally was taught java in school, and know a lot of the internal libraries and data structures, I chose it to so the project.

So I used a combination of 2 data structures for this project. Heaps and maps. Because the idea is only to keep track of the first X number
of best priced stocks for both buying and selling, I choose to keep a min and max heaps for buying at the cheapest and selling at the highest.
I used a map of the order's id, to all of it's associated metadata to Keep track of the individual orders.

When the user added an order, it's added to both the map, O(1) and the heap O(logn). The complexity of actually doing the
calculations would be O(logn) on the speed of the lookups for N times. N being the targetNumber given the assumption each order had
exactly 1 stock.

When the user reduces an order its an O(1) process at best because it updated that orders number in the map and then updated the global counter
for either the current asks or bids (or skips everything if the order does not exist). If the stock is reduced to 0 or less it becomes a O(logn) process,
n being the number of elements currently in the heap, executed n times (at the worst). This is because the heap needs to be iterated over the queue to
find the correct id to remove based on the newly updated orderbook. doing this process each time a reduce is called keeps Both heaps and the orderbook
pruned as much as possible such that the size of both heaps=the size of the orderbook.


The main bottle neck on this solution is the pruning process. You would need to add a remove(element) function which basically would transform it to an array
and then doing a bst search on a combination of price and id. Since it is possible for early data to impact late output that kinda eliminated the possibility
of breaking the log into chunks and then throwing a bunch of threads at it to process it in parallel. Though any time related constraints that would make data stale could
be used to prune  data further.