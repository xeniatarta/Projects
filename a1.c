#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <errno.h>

#define VARIANT_NUMBER "90142"
#define MAGIC_STR "mO47"
#define MIN_VERSION 13
#define MAX_VERSION 121
#define VALID_SECT_NR1 2
#define VALID_SECT_NR_MIN 4
#define VALID_SECT_NR_MAX 10
#define SECTION_HEADER_SIZE 30

int valid_section_types[] = {66, 46, 11, 85, 71, 67, 53};

typedef struct {
    char name[19];
    int type;
    int offset;
    int size;
} SectionHeader;

typedef struct {
    unsigned char version;
    unsigned char no_sections;
    SectionHeader *sections;
    unsigned short header_size;
    char magic[5];
} SFHeader;

// Citeste exact count bytes din fisierul deschid si ii stocheaza in buffer, retarnand nr total de bytes
ssize_t read_all(int fd, void *buf, size_t count) {
    size_t total = 0;
    ssize_t n;
    char *ptr = (char*)buf;
    while(total < count) {
        n = read(fd, ptr + total, count - total);
        if(n <= 0) 
            break;
        total += n;
    }
    return total;
}

// Verificam daca un tip de sectiune este valid
int is_valid_section_type(int type) {
    for(int i = 0; i < 7; i++)
        if(type == valid_section_types[i]) 
            return 1;
    return 0;
}


// Pointer la un buffer de bytes si ii interpreteaza primii sizeof(int) bytes ca un int
int read_int(unsigned char *buf) { 
    return *(int*)buf; 
}

// Interpreteaza primii sizeof(int) bytes ca un int fara semn
unsigned short read_ushort(unsigned char *buf) { 
    return *(unsigned short*)buf; 
}

// Citeste un fisier SF si parseaza header ul in structura hdr
int parse_sf_header(const char *path, SFHeader *hdr) {
    int fd = open(path, O_RDONLY);
    if(fd < 0) 
        return -1; // file open error

    // Citeste magic number si dim header din capat
    off_t file_size = lseek(fd, 0, SEEK_END);
    if(file_size < 6) { 
        close(fd); 
        return -2; // file too small
    }

    //Verif magic number si aloca memorie pentru header
    unsigned char tail[6];
    lseek(fd, file_size - 6, SEEK_SET);
    if(read_all(fd, tail, 6) != 6) { 
        close(fd); 
        return -2; // read error
    }

    hdr->header_size = read_ushort(tail);
    memcpy(hdr->magic, tail + 2, 4);
    hdr->magic[4] = '\0';

    if(strcmp(hdr->magic, MAGIC_STR) != 0) { 
        close(fd); 
        return -3; // wrong magic
    }

    off_t header_start = file_size - hdr->header_size;
    if(header_start < 0) { 
        close(fd); 
        return -2; // invalid header size
    }

    lseek(fd, header_start, SEEK_SET);
    unsigned char *buffer = malloc(hdr->header_size);
    if(!buffer || read_all(fd, buffer, hdr->header_size) != hdr->header_size) {
        free(buffer); 
        close(fd); 
        return -2; // read error
    }

    // Valideaza versiunea si nr de scetiuni
    hdr->version = buffer[0];
    hdr->no_sections = buffer[1];

    if(hdr->version < MIN_VERSION || hdr->version > MAX_VERSION) {
        free(buffer); 
        close(fd);
        return -4; // wrong version
    }

    if(!(hdr->no_sections == VALID_SECT_NR1 || 
       (hdr->no_sections >= VALID_SECT_NR_MIN && hdr->no_sections <= VALID_SECT_NR_MAX))) {
        free(buffer); 
        close(fd);
        return -5; // wrong section count
    }

    hdr->sections = malloc(hdr->no_sections * sizeof(SectionHeader));
    if(!hdr->sections) { 
        free(buffer); 
        close(fd); 
        return -1; // memory error
    }

    // Parseaza fiecare sectiune din header
    unsigned char *p = buffer + 2;
    for(int i = 0; i < hdr->no_sections; i++) {
        memcpy(hdr->sections[i].name, p, 18);
        hdr->sections[i].name[18] = '\0';
        p += 18;
        hdr->sections[i].type = read_int(p);
        if(!is_valid_section_type(hdr->sections[i].type)) {
            free(buffer); 
            free(hdr->sections); 
            close(fd); 
            return -6; // wrong section type
        }
        p += 4;
        hdr->sections[i].offset = read_int(p);
        p += 4;
        hdr->sections[i].size = read_int(p);
        p += 4;
    }
    free(buffer);
    close(fd);
    return 0;
}


void print_variant() { 
    printf("%s\n", VARIANT_NUMBER); 
}

void mode_parse(const char *path) {
    SFHeader hdr;
    int ret = parse_sf_header(path, &hdr);
    if(ret == -1) {
        printf("ERROR\ninvalid file\n");
    }
    else if(ret == -2) {
        printf("ERROR\ninvalid file\n"); 
    }
    else if(ret == -3) {
        printf("ERROR\nwrong magic\n");
    }
    else if(ret == -4) {
        printf("ERROR\nwrong version\n");
    }
    else if(ret == -5) {
        printf("ERROR\nwrong sect_nr\n");
    }
    else if(ret == -6) {
        printf("ERROR\nwrong sect_types\n");
    }
    else if(ret < 0) {
        printf("ERROR\ninvalid file\n");
    }
    else {
        printf("SUCCESS\nversion=%d\nnr_sections=%d\n", hdr.version, hdr.no_sections);
        for(int i = 0; i < hdr.no_sections; i++)
            printf("section%d: %s %d %d\n", i+1, hdr.sections[i].name, hdr.sections[i].type, hdr.sections[i].size);
        free(hdr.sections);
    }
}

void build_full_path(char *fullpath, size_t size, const char *dir, const char *name) {
    size_t dir_len = strlen(dir);
    if (dir_len > 0 && dir[dir_len - 1] == '/') {
        snprintf(fullpath, size, "%s%s", dir, name);
    } else {
        snprintf(fullpath, size, "%s/%s", dir, name);
    }
}

void process_entry(const char *path, const char *name, long size_gt, int has_perm) {
    char fullpath[1024];
    build_full_path(fullpath, sizeof(fullpath), path, name);

    struct stat st;
    if(stat(fullpath, &st)) 
        return;

    // dam skip la directoarele speciale
    if(strcmp(name, ".") == 0 || strcmp(name, "..") == 0)
        return;

    if(S_ISDIR(st.st_mode)) {
        // Pt directoare vedem doar permisiunea de write
        if(!has_perm || (st.st_mode & S_IWUSR))
            printf("%s\n", fullpath);
    } else if(S_ISREG(st.st_mode)) {
        // Pt fisiere verif si size ul si permisiunea
        int size_ok = (size_gt != -1 && st.st_size > size_gt);
        int perm_ok = (!has_perm || (st.st_mode & S_IWUSR));
        
        if(size_ok && perm_ok)
            printf("%s\n", fullpath);
    }
}


void list_directory(const char *dirPath, int recursive, long sizeGreater, int checkWritePerm) {
    DIR *dir = opendir(dirPath);
    if (!dir) return;

    struct dirent *entry;
    while ((entry = readdir(dir))) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) 
            continue;

        char fullPath[1024];
        snprintf(fullPath, sizeof(fullPath), "%s/%s", dirPath, entry->d_name);

        struct stat st;
        if (stat(fullPath, &st)) continue;

        if (S_ISDIR(st.st_mode)) {
            // Pt directoare facem recursiv
            if (recursive) {
                list_directory(fullPath, recursive, sizeGreater, checkWritePerm);
            }
            // Nu includem directoare cand size_greater este specificat
            if (sizeGreater == -1) {
                // Includem directoare cand nu filtram dupa size
                if (!checkWritePerm || (st.st_mode & S_IWUSR)) {
                    printf("%s\n", fullPath);
                }
            }
        } 
        else if (S_ISREG(st.st_mode)) {
            // Pt fisiere verif si size si permisiunea
            if ((sizeGreater == -1 || st.st_size > sizeGreater) &&
                (!checkWritePerm || (st.st_mode & S_IWUSR))) {
                printf("%s\n", fullPath);
            }
        }
    }
    closedir(dir);
}

void mode_list(int recursive, long size_gt, int has_perm, const char *path) {
    // Verif daca directorul exista
    struct stat st;
    if (stat(path, &st) || !S_ISDIR(st.st_mode)) {
        printf("ERROR\ninvalid directory path\n");
        return;
    }

    printf("SUCCESS\n");
    list_directory(path, recursive, size_gt, has_perm);
}
  
// Extrage o linie specifica dintr-o sectiune a fisierului SF
void mode_extract(const char *path, int sect, int line) {
    // Parseaza header ul
    SFHeader hdr;
    int ret = parse_sf_header(path, &hdr);
    if(ret < 0 || sect < 1 || sect > hdr.no_sections) {
        printf("ERROR\ninvalid file|section|line\n");
        if(ret >= 0) free(hdr.sections);
        return;
    }

    // Citeste continutul sectiunii
    SectionHeader *sec = &hdr.sections[sect-1];
    int fd = open(path, O_RDONLY);
    if(fd < 0) { 
        free(hdr.sections); 
        printf("ERROR\ninvalid file|section|line\n");
        return; 
    }

    char *buf = malloc(sec->size + 1);
    if(!buf) {
        close(fd);
        free(hdr.sections);
        printf("ERROR\ninvalid file|section|line\n");
        return;
    }
    
    lseek(fd, sec->offset, SEEK_SET);
    if(read_all(fd, buf, sec->size) != sec->size) {
        free(buf); 
        close(fd); 
        free(hdr.sections); 
        printf("ERROR\ninvalid file|section|line\n");
        return;
    }
    buf[sec->size] = '\0';

    // Numara liniile in sectiune
    int line_count = 0;
    int line_starts[sec->size]; // Nr maxim de linii posibile
    
    line_starts[line_count++] = 0;
    
    for(int i = 0; i < sec->size; i++) {
        if(buf[i] == '\n') {
            if(i + 1 < sec->size) { // Nu ne aflam inca la finalul sectiunii
                line_starts[line_count++] = i + 1;
            }
        }
    }
    
    // Extrage linia ceruta
    if(line < 1 || line > line_count) {
        printf("ERROR\ninvalid file|section|line\n");
    } else {
        // Gasim inceputul si sfarsitul liniei
        int line_idx = line_count - line;
        int start = line_starts[line_idx];
        int end;
        if (line_idx + 1 < line_count) {
             end = line_starts[line_idx + 1] - 1;
            } else {
                end = sec->size;
                }
        
        // Calculam lungimea si o afisam
        int len = end - start;
        char *line_content = malloc(len + 1);
        if(line_content) {
            memcpy(line_content, buf + start, len);
            line_content[len] = '\0';
            printf("SUCCESS\n%s\n", line_content);
            free(line_content);
        } else {
            printf("ERROR\ninvalid file|section|line\n");
        }
    }

    free(buf); 
    close(fd); 
    free(hdr.sections);
}


void find_sf_files(const char *path) {
    DIR *dir = opendir(path);
    if (!dir) {
        return;
    }

    struct dirent *entry;
    while ((entry = readdir(dir))) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0)
            continue;

        char fullpath[1024];
        build_full_path(fullpath, sizeof(fullpath), path, entry->d_name);

        struct stat st;
        if (stat(fullpath, &st)) {
            continue;
        }

        if (S_ISDIR(st.st_mode)) {
            find_sf_files(fullpath);
        } else if (S_ISREG(st.st_mode)) {
            SFHeader hdr;
            if (parse_sf_header(fullpath, &hdr) == 0) {
                int valid = 0;
                for (int i = 0; i < hdr.no_sections && !valid; i++) {
                    int fd = open(fullpath, O_RDONLY);
                    if (fd < 0) 
                        continue;

                    char *buffer = malloc(hdr.sections[i].size);
                    lseek(fd, hdr.sections[i].offset, SEEK_SET);
                    ssize_t bytes_read = read_all(fd, buffer, hdr.sections[i].size);
                    close(fd);

                    if (bytes_read != hdr.sections[i].size) {
                        free(buffer);
                        continue;
                    }

                    int lines = 0;
                    for (int j = 0; j < hdr.sections[i].size; j++) {
                        if (buffer[j] == '\n') {
                            lines++;
                        }
                    }
                    free(buffer);

                    if (lines >= 16) { // Cel puțin 17 linii (16 separatori)
                        valid = 1;
                    }
                }

                if (valid) 
                    printf("%s\n", fullpath);
                
                free(hdr.sections);
            }
        }
    }
    closedir(dir);
}

// Cauta toate fisierele SF valide care contin sectiuni cu peste 16 linii
void mode_findall(const char *path) {
    struct stat st;
    if(stat(path, &st) || !S_ISDIR(st.st_mode)) {
        printf("ERROR\ninvalid directory path\n");
        return;
    }
    printf("SUCCESS\n");
    find_sf_files(path);
}

int main(int argc, char **argv) {
    if(argc < 2) 
        return 1;

    if(!strcmp(argv[1], "variant")) 
        print_variant();
    else if(!strcmp(argv[1], "list")) {
        int recursive = 0, has_perm = 0;
        long size_gt = -1;
        char *path = NULL;

        for(int i = 2; i < argc; i++) {
            if(!strcmp(argv[i], "recursive")) 
                recursive = 1;
            else if(!strncmp(argv[i], "path=", 5)) 
                path = argv[i] + 5;
            else if(!strncmp(argv[i], "size_greater=", 13)) 
                size_gt = atol(argv[i] + 13);
            else if(!strcmp(argv[i], "has_perm_write")) 
                has_perm = 1;
        }

        if(!path) { 
            printf("ERROR\ninvalid directory path\n"); 
            return 1; 
        }
        mode_list(recursive, size_gt, has_perm, path);
    }
    else if(!strcmp(argv[1], "parse")) {
        char *path = NULL;
        for(int i = 2; i < argc; i++)
            if(!strncmp(argv[i], "path=", 5)) 
                path = argv[i] + 5;
        if(!path) { 
            printf("ERROR\ninvalid file path\n"); 
            return 1; 
        }
        mode_parse(path);
    }
    else if(!strcmp(argv[1], "extract")) {
        char *path = NULL;
        int sect = 0, line = 0;
        for(int i = 2; i < argc; i++) {
            if(!strncmp(argv[i], "path=", 5)) 
                path = argv[i] + 5;
            else if(!strncmp(argv[i], "section=", 8)) 
                sect = atoi(argv[i] + 8);
            else if(!strncmp(argv[i], "line=", 5)) 
                line = atoi(argv[i] + 5);
        }
        if(!path || sect < 1 || line < 1) { 
            printf("ERROR\ninvalid file|section|line\n"); 
            return 1; 
        }
        mode_extract(path, sect, line);
    }
    else if(!strcmp(argv[1], "findall")) {
        char *path = NULL;
        for(int i = 2; i < argc; i++)
            if(!strncmp(argv[i], "path=", 5)) 
                path = argv[i] + 5;
        if(!path) { printf("ERROR\ninvalid directory path\n"); 
            return 1; 
        }
        mode_findall(path);
    }
    else 
        return 1;

    return 0;
}