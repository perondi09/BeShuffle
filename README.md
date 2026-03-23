# 📊 BeShuffle

Projeto criado para ajudar pessoas curiosas como eu. BeShuffle é uma aplicação que todo dia indica um álbum novo para você conhecer novos artistas e estilos musicais.

## 🚀 Tecnologias Utilizadas

Este projeto foi desenvolvido utilizando as seguintes tecnologias:

- **Java 21**
- **Spring Boot 3**
- **Maven** para gerenciamento de dependências

## ⚙️ Pré-requisitos

Antes de começar, você vai precisar ter as seguintes ferramentas instaladas na sua máquina:

- [JDK 21+](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Maven 3.8.1+](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/)

## 🛠️ Configuração e Execução

1. Clone o repositório:
```bash
git clone https://github.com/perondi09/beshuffle.git
```

2. Acesse a pasta do projeto:
```bash
cd beshuffle
```

## 📍 Endpoints da Aplicação

### Registrar Álbum do Dia

```http
POST /api/albums/today
```

Registra ou atualiza o álbum indicado para o dia atual.

**Resposta de sucesso `200 OK`:**
```json
{
    "id": 1,
    "spotifyAlbumId": "0I8i8NKTztZQJiQCkUSA63",
    "albumName": "INDUSTRY MACHINE",
    "artistName": "ODUMODUBLVCK",
    "imageUrl": "https://i.scdn.co/image/ab67616d0000b273739fcfc70e236250bbe577d1",
    "albumUrl": "spotify:album:0I8i8NKTztZQJiQCkUSA63",
    "releaseDate": "2025-10-05",
    "displayDate": "2026-03-22"
}
```

| Campo            | Tipo      | Descrição                                    |
| :--------------- | :-------- | :------------------------------------------- |
| `id`             | `Integer` | Identificador interno do registro            |
| `spotifyAlbumId` | `String`  | ID do álbum no Spotify                       |
| `albumName`      | `String`  | Nome do álbum                                |
| `artistName`     | `String`  | Nome do artista                              |
| `imageUrl`       | `String`  | URL da capa do álbum                         |
| `albumUrl`       | `String`  | URI do álbum no Spotify                      |
| `releaseDate`    | `String`  | Data de lançamento original do álbum         |
| `displayDate`    | `String`  | Data em que o álbum foi exibido no BeShuffle |

---

### Buscar Álbum do Dia

```http
GET /api/albums/today
```

Retorna o álbum indicado para o dia atual.

**Resposta de sucesso `200 OK`:**
```json
{
    "id": 1,
    "spotifyAlbumId": "0I8i8NKTztZQJiQCkUSA63",
    "albumName": "INDUSTRY MACHINE",
    "artistName": "ODUMODUBLVCK",
    "imageUrl": "https://i.scdn.co/image/ab67616d0000b273739fcfc70e236250bbe577d1",
    "albumUrl": "spotify:album:0I8i8NKTztZQJiQCkUSA63",
    "releaseDate": "2025-10-05",
    "displayDate": "2026-03-22"
}
```

---

## 👨‍💻 Autor

Desenvolvido por **Guilherme Perondi** — [LinkedIn](https://www.linkedin.com/in/guilherme-perondi/)
