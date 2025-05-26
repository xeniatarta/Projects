#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include "a2_helper.h"
#include <pthread.h>
#include <semaphore.h>
#include <fcntl.h>

#define NR_THREADS 5
#define NR_THREADS_2 35
#define MAX_4_THREADS_P7 4

typedef struct {
    int id;
    sem_t* sem_1;
    sem_t* sem_3;
    sem_t* sem_1_end;
    sem_t* sem_others;
    sem_t* sem_5_1;
    sem_t* sem_5_3;
} TH_STRUCT;

typedef struct {
    sem_t sem_done;
    pthread_mutex_t lock;
    int nr_perm;
} SEM_STRUCT;

typedef struct {
    int id;
    SEM_STRUCT* sem_sim_4_threads;
    pthread_mutex_t* lock_T11;
    pthread_cond_t* cond_T11;
    int* active_threads;
} TH_STRUCT2;

typedef struct {
    int id;
    sem_t* sem_5_1;
    sem_t* sem_5_3;
} TH_STRUCT3;

sem_t *sem_5_1, *sem_5_3;

void* thread_function(void* arg) {
    TH_STRUCT *s = (TH_STRUCT*) arg;
    int current_thread = s->id + 1;

    if (current_thread == 1) {
        sem_wait(s->sem_1);
        info(BEGIN, 4, current_thread);
        sem_post(s->sem_3);
        sem_wait(s->sem_1_end);
        info(END, 4, current_thread);
        sem_post(s->sem_1);
    } 
    else if (current_thread == 3) {
        sem_wait(s->sem_3);
        info(BEGIN, 4, current_thread);
        info(END, 4, current_thread);
        sem_post(s->sem_1_end);
    } 
    else if (current_thread == 2) {
        sem_wait(s->sem_others);
        sem_wait(s->sem_5_1);
        info(BEGIN, 4, current_thread);
        info(END, 4, current_thread);
        sem_post(s->sem_5_3);
        sem_post(s->sem_others);
    } 
    else {
        sem_wait(s->sem_others);
        info(BEGIN, 4, current_thread);
        info(END, 4, current_thread);
        sem_post(s->sem_others);
    }
    return NULL;
}

void sem_init_safe(SEM_STRUCT* s, int nr_perm) {
    sem_init(&s->sem_done, 0, nr_perm);
    pthread_mutex_init(&s->lock, NULL);
    s->nr_perm = nr_perm;
}

void sem_wait_safe(SEM_STRUCT* s) {
    sem_wait(&s->sem_done);
    pthread_mutex_lock(&s->lock);
    s->nr_perm--;
    pthread_mutex_unlock(&s->lock);
}

void sem_post_safe(SEM_STRUCT* s) {
    pthread_mutex_lock(&s->lock);
    s->nr_perm++;
    pthread_mutex_unlock(&s->lock);
    sem_post(&s->sem_done);
}

void sem_destroy_safe(SEM_STRUCT* s) {
    sem_destroy(&s->sem_done);
    pthread_mutex_destroy(&s->lock);
}

void* thread_function_2(void* arg) {
    TH_STRUCT2 *s = (TH_STRUCT2*) arg;
    int current_thread = s->id + 1;

    sem_wait_safe(s->sem_sim_4_threads);
    
    pthread_mutex_lock(s->lock_T11);
    (*s->active_threads)++;
    
    if (current_thread == 11) {
        while (*s->active_threads < MAX_4_THREADS_P7) {
            pthread_cond_wait(s->cond_T11, s->lock_T11);
        }
    }
    
    pthread_mutex_unlock(s->lock_T11);

    info(BEGIN, 7, current_thread);
    info(END, 7, current_thread);

    pthread_mutex_lock(s->lock_T11);
    (*s->active_threads)--;
    pthread_cond_broadcast(s->cond_T11);
    pthread_mutex_unlock(s->lock_T11);

    sem_post_safe(s->sem_sim_4_threads);
    return NULL;
}

void* thread_function_3(void* arg) {
    TH_STRUCT3* s = (TH_STRUCT3*) arg;
    int current_thread = s->id + 1;

    if(current_thread == 3) {
        sem_wait(s->sem_5_3);
        info(BEGIN, 5, current_thread);
        info(END, 5, current_thread);
        return NULL;
    }

    info(BEGIN, 5, current_thread);

    if(current_thread == 1) {
        info(END, 5, current_thread);
        sem_post(s->sem_5_1);
        return NULL;
    }

    info(END, 5, current_thread);
    return NULL;
}

int main() {
    init();
    info(BEGIN, 1, 0);

    sem_5_1 = sem_open("/sem_5_1", O_CREAT | O_EXCL, 0644, 0);
    sem_5_3 = sem_open("/sem_5_3", O_CREAT | O_EXCL, 0644, 0);

    pid_t p2 = fork();
    if (p2 == 0) {
        info(BEGIN, 2, 0);

        pid_t p5 = fork();
        if (p5 == 0) {
            info(BEGIN, 5, 0);

            pid_t p6 = fork();
            if (p6 == 0) {
                info(BEGIN, 6, 0);

                pid_t p8 = fork();
                if (p8 == 0) {
                    info(BEGIN, 8, 0);
                    info(END, 8, 0);
                    _exit(0);
                }

                waitpid(p8, NULL, 0);
                info(END, 6, 0);
                _exit(0);
            }

            waitpid(p6, NULL, 0);

            pthread_t tids3[NR_THREADS];
            TH_STRUCT3 params3[NR_THREADS];

            for(int i = 0; i < NR_THREADS; i++) {
                params3[i].id = i;
                params3[i].sem_5_1 = sem_5_1;
                params3[i].sem_5_3 = sem_5_3;
                pthread_create(&tids3[i], NULL, thread_function_3, &params3[i]);
            }

            for (int i = 0; i < NR_THREADS; i++) {
                pthread_join(tids3[i], NULL);
            }

            info(END, 5, 0);
            _exit(0);
        }

        waitpid(p5, NULL, 0);
        info(END, 2, 0);
        _exit(0);
    }

    pid_t p3 = fork();
    if (p3 == 0) {
        info(BEGIN, 3, 0);
        info(END, 3, 0);
        _exit(0);
    }

    pid_t p4 = fork();
    if (p4 == 0) {
        info(BEGIN, 4, 0);

        pthread_t tids[NR_THREADS];
        TH_STRUCT params[NR_THREADS];
        sem_t sem_1, sem_3, sem_others, sem_1_end;
        sem_init(&sem_1, 0, 1);
        sem_init(&sem_3, 0, 0);
        sem_init(&sem_others, 0, 3);
        sem_init(&sem_1_end, 0, 0);

        for (int i = 0; i < NR_THREADS; i++) {
            params[i].id = i;
            params[i].sem_1 = &sem_1;
            params[i].sem_3 = &sem_3;
            params[i].sem_others = &sem_others;
            params[i].sem_1_end = &sem_1_end;
            params[i].sem_5_1 = sem_5_1;
            params[i].sem_5_3 = sem_5_3;
            pthread_create(&tids[i], NULL, thread_function, &params[i]);
        }

        for (int i = 0; i < NR_THREADS; i++) {
            pthread_join(tids[i], NULL);
        }

        sem_destroy(&sem_1);
        sem_destroy(&sem_3);
        sem_destroy(&sem_others);
        sem_destroy(&sem_1_end);

        info(END, 4, 0);
        _exit(0);
    }

    pid_t p7 = fork();
    if (p7 == 0) {
        info(BEGIN, 7, 0);

        TH_STRUCT2 params2[NR_THREADS_2];
        pthread_t tids2[NR_THREADS_2];
        SEM_STRUCT sem_sim_4_thread;
        pthread_mutex_t lock_T11 = PTHREAD_MUTEX_INITIALIZER;
        pthread_cond_t cond_T11 = PTHREAD_COND_INITIALIZER;
        int active_threads = 0;

        sem_init_safe(&sem_sim_4_thread, 4);

        for (int i = 0; i < NR_THREADS_2; i++) {
            params2[i].id = i;
            params2[i].cond_T11 = &cond_T11;
            params2[i].lock_T11 = &lock_T11;
            params2[i].sem_sim_4_threads = &sem_sim_4_thread;
            params2[i].active_threads = &active_threads;
            pthread_create(&tids2[i], NULL, thread_function_2, &params2[i]);
        }

        for (int i = 0; i < NR_THREADS_2; i++) {
            pthread_join(tids2[i], NULL);
        }

        pthread_mutex_destroy(&lock_T11);
        pthread_cond_destroy(&cond_T11);
        sem_destroy_safe(&sem_sim_4_thread);

        info(END, 7, 0);
        _exit(0);
    }

    waitpid(p2, NULL, 0);
    waitpid(p3, NULL, 0);
    waitpid(p4, NULL, 0);
    waitpid(p7, NULL, 0);

    sem_close(sem_5_1);
    sem_close(sem_5_3);
    sem_unlink("/sem_5_1");
    sem_unlink("/sem_5_3");

    info(END, 1, 0);
    return 0;
}