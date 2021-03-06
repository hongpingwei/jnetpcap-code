/**
 * Copyright (C) 2009 Sly Technologies, Inc. This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.jnetpcap.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

/**
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class PcapPacketArrayList
    implements
    List<PcapPacket>,
    RandomAccess,
    Serializable,
    PcapPacketHandler<Object> {

	/**
	 * Autogenerated
	 */
	private static final long serialVersionUID = -6379668946303974430L;

	private final ArrayList<PcapPacket> list;

	public PcapPacketArrayList() {
		list = new ArrayList<PcapPacket>();
	}

	public PcapPacketArrayList(int initialCapacity) {
		list = new ArrayList<PcapPacket>(initialCapacity);
	}

	public PcapPacketArrayList(Collection<? extends PcapPacket> collection) {
		list = new ArrayList<PcapPacket>(collection);
	}

	/**
	 * @param index
	 * @param element
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	public void add(int index, PcapPacket element) {
		this.list.add(index, element);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean add(PcapPacket o) {
		return this.list.add(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends PcapPacket> c) {
		return this.list.addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends PcapPacket> c) {
		return this.list.addAll(index, c);
	}

	/**
	 * @see java.util.ArrayList#clear()
	 */
	public void clear() {
		this.list.clear();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#clone()
	 */
	public Object clone() {
		return this.list.clone();
	}

	/**
	 * @param elem
	 * @return
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	public boolean contains(Object elem) {
		return this.list.contains(elem);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return this.list.containsAll(c);
	}

	/**
	 * @param minCapacity
	 * @see java.util.ArrayList#ensureCapacity(int)
	 */
	public void ensureCapacity(int minCapacity) {
		this.list.ensureCapacity(minCapacity);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return this.list.equals(o);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public PcapPacket get(int index) {
		return this.list.get(index);
	}

	/**
	 * @return
	 * @see java.util.AbstractList#hashCode()
	 */
	public int hashCode() {
		return this.list.hashCode();
	}

	/**
	 * @param elem
	 * @return
	 * @see java.util.ArrayList#indexOf(java.lang.Object)
	 */
	public int indexOf(Object elem) {
		return this.list.indexOf(elem);
	}

	/**
	 * @return
	 * @see java.util.ArrayList#isEmpty()
	 */
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractList#iterator()
	 */
	public Iterator<PcapPacket> iterator() {
		return this.list.iterator();
	}

	/**
	 * @param elem
	 * @return
	 * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object elem) {
		return this.list.lastIndexOf(elem);
	}

	/**
	 * @return
	 * @see java.util.AbstractList#listIterator()
	 */
	public ListIterator<PcapPacket> listIterator() {
		return this.list.listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.AbstractList#listIterator(int)
	 */
	public ListIterator<PcapPacket> listIterator(int index) {
		return this.list.listIterator(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	public PcapPacket remove(int index) {
		return this.list.remove(index);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return this.list.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return this.list.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return this.list.retainAll(c);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	public PcapPacket set(int index, PcapPacket element) {
		return this.list.set(index, element);
	}

	/**
	 * @return
	 * @see java.util.ArrayList#size()
	 */
	public int size() {
		return this.list.size();
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.AbstractList#subList(int, int)
	 */
	public List<PcapPacket> subList(int fromIndex, int toIndex) {
		return this.list.subList(fromIndex, toIndex);
	}

	/**
	 * @return
	 * @see java.util.ArrayList#toArray()
	 */
	public Object[] toArray() {
		return this.list.toArray();
	}

	/**
	 * @param <T>
	 * @param a
	 * @return
	 * @see java.util.ArrayList#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		return this.list.toArray(a);
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return this.list.toString();
	}

	/**
	 * @see java.util.ArrayList#trimToSize()
	 */
	public void trimToSize() {
		this.list.trimToSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnetpcap.packet.PcapPacketHandler#nextPacket(org.jnetpcap.packet.PcapPacket,
	 *      java.lang.Object)
	 */
	public void nextPacket(PcapPacket packet, Object user) {
		list.add(packet);
	}
}
