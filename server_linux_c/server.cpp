#include <sys/types.h>
#include <sys/socket.h>
#include <errno.h>
#include <cstdio>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <cstdlib>
#include <fcntl.h>
#include <sys/shm.h>
#include <time.h>
#include <signal.h>
#include <sys/prctl.h>
#include <fstream>
#include <iostream>
//#define MYPORT  8887

#define QUEUE   512
#define BUFFER_SIZE 32768
#define STR_SIZE 16384
using namespace std;

void Set_Reuse(int socketfd)
{
	int on = 1;
	if (setsockopt(socketfd, SOL_SOCKET, SO_REUSEADDR, (char *)&on, sizeof(on)) < 0) {
		printf("errno=%d(%s)\n", errno, strerror(errno));
		close(socketfd);
		exit(1);
	}
}

void Set_Nonblock(int socketfd)
{
	int val;
	if ((val = fcntl (socketfd, F_GETFL, 0)) < 0) {
		printf("errno=%d(%s)\n", errno, strerror(errno));
		close(socketfd);
		exit(1);
	}
	if (fcntl(socketfd, F_SETFL, val | O_NONBLOCK) < 0) {
		printf("errno=%d(%s)\n", errno, strerror(errno));
		close(socketfd);
		exit(1);
	}
}


int main(int argc, char**argv)
{
	if (argc != 2)
	{
		printf("wrong para num\n");
		return -1;
	}

	//define sockfd
	socklen_t client_len, server_len;
	int server_sockfd, client_sockfd;
	server_sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (server_sockfd < 0)
	{
		printf("error in create server_sockfd\n");
		return -1;
	}
	/**para:family,type,protocol
	   family:  AF_INET for IPv4
	   type:    SOCK_STREAM for tcp,SOCK_DGRAM for udp
	   protocol:0 usually**/
	//allow reuse of localaddr and binding
	Set_Reuse(server_sockfd);
	//switch to non-block
	Set_Nonblock(server_sockfd);
	//define sockaddr_in
	struct sockaddr_in server_sockaddr, client_sockaddr;
	bzero(&server_sockaddr, sizeof(server_sockaddr));
	bzero(&client_sockaddr, sizeof(client_sockaddr));
	server_sockaddr.sin_family = AF_INET;
	server_sockaddr.sin_port = htons(atoi(argv[1]));
	//server_sockaddr.sin_port = htons(MYPORT);
	server_sockaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_len = sizeof(server_sockaddr);
	client_len = sizeof(client_sockaddr);

	//bind，seccess return 0，error return-1
	if (bind(server_sockfd, (struct sockaddr *)&server_sockaddr, server_len) == -1)
	{
		perror("bind");
		close(server_sockfd);
		exit(1);
	}

	//listen，success return 0，error return -1
	if (listen(server_sockfd, QUEUE) == -1)
	{
		perror("listen");
		close(server_sockfd);
		exit(1);
	}

	printf("bind&listen success\n");
	//client socket
	//success return non-negetive int ,error return -1
	fd_set recv_flags,send_flags;

	struct timeval timer;
	timer.tv_sec = 1;
	timer.tv_usec = 0;
	int sel;
	acceptclient:
	while (1)
	{
		FD_ZERO(&recv_flags);
		FD_SET(server_sockfd, &recv_flags);
		sel = select(server_sockfd + 1, &recv_flags, NULL, NULL, &timer);
		if (sel > 0)
			break;
	}
	client_sockfd = accept(server_sockfd, (struct sockaddr*)&client_sockaddr, &client_len);
	if (client_sockfd <= 0)
	{
		printf("wrong client socket\n");
		goto acceptclient;
	}
	else printf("accept from ip:%s\n", inet_ntoa(client_sockaddr.sin_addr));
	char recvbuffer[BUFFER_SIZE];
	char sendbuffer[BUFFER_SIZE];
	int len;
	FD_ZERO(&recv_flags);
	FD_SET (client_sockfd, &recv_flags);
	if  (select(client_sockfd + 1, &recv_flags, NULL, (fd_set*)0, NULL)) {

		FD_CLR(client_sockfd, &recv_flags);
		len=recv(client_sockfd, recvbuffer , BUFFER_SIZE, 0);
		ofstream out ("input.cpp", ios::out|ios::binary|ios::trunc);  
		if (!out)
		{
			cout<<"can't open input.cpp"<<endl;
			goto acceptclient;
		}
		out.write(recvbuffer,len);
		out.close();

	}

	system("g++ input.cpp -o runfile >>result.dat");
	ifstream in("runfile", ios::in|ios::binary);  //查看可执行文件是否存在
	if (!in)
	{
		cout<<"can't open result.dat"<<endl;
		goto sendresult;
	}
	in.close();
	system("./runfile >>result.dat");
	sendresult:
	FD_ZERO(&send_flags);
	FD_SET( client_sockfd, &send_flags);
	if (select(client_sockfd + 1, NULL, &send_flags, (fd_set*)0, NULL) > 0) {
		FD_CLR(client_sockfd, &send_flags);
		char* buffer;
		ifstream in ("result.dat", ios::in|ios::binary|ios::ate);  
        int size = in.tellg();  
        in.seekg (0, ios::beg);  
        buffer = new char [size];  
        in.read (buffer, size);  
        in.close();  
		len = send(client_sockfd, buffer, size, 0);
		if (buffer)
			delete buffer;
	}
	close(client_sockfd);
	remove("runfile");
	remove("result.dat");
	goto acceptclient;
	printf("close server\n");
	return 0;
}
