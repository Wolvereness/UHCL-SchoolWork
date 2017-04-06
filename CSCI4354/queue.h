/*
 * Copyright (C) 2017  Wesley Wolfe
 * Works provided with supplemented terms, outlined in accompanying
 * documentation, or found at
 * https://github.com/Wolvereness/UHCL-ScholWork
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
typedef struct {
	int length;
	int capacity;
	int element_size;
	// The "original" location
	void *memory;
} Queue_Internal;

#define init_queue(P) (init_queue_internal((void **) &P, sizeof (P[0])))
#define queue_enqueue(P, T) (enqueue_queue_internal((void **) &P, &T))
#define queue_dequeue(P, T) (dequeue_queue_internal((void **) &P, T))
#define delete_queue(P) (delete_queue_internal((void **) &P))
#define queue_length(P) (length_queue_internal((const void **) &P))

void init_queue_internal(void ** queue, int element_size) {
	#define QUEUE_INTERNAL_DEFAULT_SIZE 10
	Queue_Internal data;

	data.length = 0;
	data.capacity = QUEUE_INTERNAL_DEFAULT_SIZE;
	data.element_size = element_size;
	data.memory = malloc(
		sizeof(Queue_Internal)
		+ data.capacity * data.element_size
		);

	Queue_Internal* mem = data.memory;
	mem[0] = data;
	*queue = &mem[1];
}

void dequeue_queue_internal(void ** queue, void *element) {
	Queue_Internal* mem = *queue;
	Queue_Internal data = mem[-1];

	memcpy(element, mem, data.element_size);
	data.length -= 1;
	*queue = mem = ((void *) mem) + data.element_size;
	mem[-1] = data;
}

void enqueue_queue_internal(void ** queue, const void *element) {
	#define PHI 1.6180339887498948482
	Queue_Internal* mem = *queue;
	Queue_Internal data = mem[-1];

	if (
		((void *) mem) + data.length * data.element_size
		>= data.memory + sizeof(Queue_Internal) + data.capacity * data.element_size
		)
	{
		// Insufficient size to push to the end
		if (((int) data.length * PHI) > data.capacity) {
			// Resize
			void *old = data.memory;
			data.memory = realloc(
				old,
				sizeof(Queue_Internal)
				+ ((int) data.capacity * PHI) * data.element_size
				);
			data.capacity = (int) data.capacity * PHI;
			mem = ((void *) mem) - old + data.memory;
		} else {
			// Shift
			memmove(
				data.memory,
				&mem[-1],
				sizeof(Queue_Internal) + data.length * data.element_size
				);
			mem = data.memory + sizeof(Queue_Internal);
		}
		*queue = mem;
	}
	memcpy(
		((void *) mem) + data.length * data.element_size,
		element,
		data.element_size
		);
	data.length += 1;
	mem[-1] = data;
}

void delete_queue_internal(void ** queue) {
	Queue_Internal* mem = *queue;
	free(mem[-1].memory);
}

int length_queue_internal(const void ** queue) {
	const Queue_Internal* mem = *queue;
	Queue_Internal data = mem[-1];
	return data.length;
}
