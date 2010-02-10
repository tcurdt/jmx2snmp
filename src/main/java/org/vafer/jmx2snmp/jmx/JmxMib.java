package org.vafer.jmx2snmp.jmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The JmxMib represents a walk-able SNMP OID tree. The tree is loaded and
 * constructed from a load(Reader). The expect format include a definition
 * for the root node as "<oid>" and all other nodes.
 * 
 * 1.3.6.1.4.1.27305 = <oid>
 * 1.3.6.1.4.1.27305.12 = bean1
 * 1.3.6.1.4.1.27305.12.1 = AnotherInt
 * 1.3.6.1.4.1.27305.12.2 = SomeColor
 * 1.3.6.1.4.1.27305.12.3 = TheBoolean
 * 
 * @threadsafe yes
 */
public final class JmxMib {

	private final AtomicReference<Node> root = new AtomicReference<Node>(new Node(null, 0, null));

	public static class Bean {
		public boolean leaf;
		public String relativePath;
		public String absolutePath;
	};
	
	private static class Node {

		public final Node parent;
		public final int idx;
		public String value;
		public final SortedMap<Integer, Node> childs = new TreeMap<Integer, Node>();
		
		public Node(Node parent, int idx, String value) {
			this.parent = parent;
			this.idx = idx;
			this.value = value;
		}
		
		public String getOid() {
			if (parent == null) {
				return null;
			}

			final String parentOid = parent.getOid();
			
			if (parentOid == null) {
				return "" + idx;
			}
			
			return parentOid + '.' + idx;
		}

		public String getPath() {
			if (parent == null) {
				return null;
			}
			
			final String parentPath = parent.getPath();

			if (parentPath == null) {
				return value;
			}
			
			return parentPath + '.' + value;
		}
		
//		public Node find(String path) {
//			if (path.equals(this.getPath())) {
//				return this;
//			}
//			
//			for(Node child : childs.values()) {
//				final Node node = child.find(path);
//				if (node != null) {
//					return node;
//				}
//			}
//			
//			return null;
//		}
		
		public Node getNext() {
			Node leaf = this;
			while(leaf.childs.size() > 0) {
				leaf = leaf.childs.get(leaf.childs.firstKey());
			}
			
			if (leaf != this) {
				return leaf;
			}
			
			Node n = leaf;
			while(n != null) {
				final Node p = n.parent;
				
				if (p == null) {
					return null;
				}
				
				final Node sibling = p.childAfter(n.idx);

				if (sibling != null) {
					if (sibling.childs.size() > 0) {
						return sibling.getNext();
					}
					return sibling;
				}

				n = p;
			}
			
			return null;			
		}
		
		public Node childAfter(int index) {
			
			Iterator<Integer> iterator = childs.keySet().iterator();
			while (iterator.hasNext()) {
				Integer key = iterator.next();
				if (key > index) {
					return childs.get(key);
				}
			}
			
			return null;
		}
	
		public int getNodeCount() {			
			int count = 1;
			for(Node node : childs.values()) {
				count += node.getNodeCount();
			}
			return count;
		}
	}
		
	private Node createNode( Node root, String oid ) {
		final String[] indexes = oid.split("\\.");
		Node node = root;
		for (int i = 0; i < indexes.length; i++) {
			
			final Integer idx = Integer.parseInt(indexes[i]);
			
			Node n = node.childs.get(idx);
			if (n == null) {
				n = new Node(node, idx, null);
				node.childs.put(idx, n);
			}
			
			node = n;
		}
		
		return node;
	}

	private Node lookupNode( Node root, String oid ) {
		final String[] indexes = oid.split("\\.");
		Node node = root;
		for (int i = 0; i < indexes.length; i++) {
			
			final Integer idx = Integer.parseInt(indexes[i]);
			
			Node n = node.childs.get(idx);
			if (n == null) {
				return null;
			}
			
			node = n;
		}
		
		return node;
	}

	public synchronized void load(Reader pConfigReader) throws IOException {		
		final BufferedReader br = new BufferedReader(pConfigReader);

		final Node newRoot = new Node(null, 0, null);
		
		while(true) {
			final String line = br.readLine();
			
			if (line == null) {
				break;
			}
			
			final String[] tokens = line.split("=");
			final String key = tokens[0].trim();
			final String value = tokens[1].trim();
		  
  		  	final Node node = createNode(newRoot, key);
  		  	node.value = value;	  		  	
		}

		br.close();
		
		root.set(newRoot);
	}
	
	public String getPathFromOid(String oid) {
		final Node node = lookupNode(root.get(), oid);

		if (node == null) {
			return null;
		}
		
		return node.getPath();
	}

	public String getNextOidFromOid(String oid) {
		final Node node = lookupNode(root.get(), oid);
		
		if (node == null) {
			return null;
		}
		
		final Node nextNode = node.getNext();
		
		if (nextNode == null) {
			return null;
		}
		
		return nextNode.getOid();
	}

	public int getNodeCount() {
		return root.get().getNodeCount() - 1;
	}
	
	private void fillMapping(Node node, TreeMap<String,Bean> map) {
		
		if (node.value != null) {
			Bean bean = new Bean();
			bean.leaf = node.childs.size() == 0;
			bean.relativePath = node.value;
			bean.absolutePath = node.getPath();
			map.put(node.getOid(), bean);			
		}
		
		for(Node child : node.childs.values()) {
			fillMapping(child, map);
		}		
	}
	
	public TreeMap<String,Bean> getMapping() {

		final TreeMap<String,Bean> map = new TreeMap<String,Bean>();
		
		final Node node = root.get();
		
		fillMapping(node, map);

		return map;
	}

}
