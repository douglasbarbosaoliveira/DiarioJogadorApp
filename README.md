# ⚽ Diário do Jogador

> Aplicativo Android nativo para gestão de carreira e performance de atletas de futebol.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2C8EBB?style=for-the-badge&logo=square&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

## 📖 Visão Geral

O **Diário do Jogador** é um aplicativo desenvolvido para jogadores de futebol amadores e profissionais gerenciarem suas atividades. O aplicativo centraliza o histórico de partidas, estatísticas de desempenho e rotinas de treinamento em uma interface intuitiva e moderna.

O projeto destaca-se por utilizar uma **Arquitetura Híbrida de Dados**:
* **Nuvem (API REST):** Para dados críticos e compartilháveis (Jogos, Treinos, Autenticação).
* **Local (SQLite):** Para dados sensíveis e configurações do dispositivo (Endereço, Telefone, Foto de Perfil).

---

## 📱 Screenshots

| Login & Auth | Dashboard | Listagem de Jogos |
|:---:|:---:|:---:|
| <img src="URL_DA_IMAGEM_LOGIN" width="200" /> | <img src="URL_DA_IMAGEM_MENU" width="200" /> | <img src="URL_DA_IMAGEM_LISTA" width="200" /> |

| Formulário de Jogo | Perfil (Câmera) | Detalhes de Treino |
|:---:|:---:|:---:|
| <img src="URL_DA_IMAGEM_FORM" width="200" /> | <img src="URL_DA_IMAGEM_PERFIL" width="200" /> | <img src="URL_DA_IMAGEM_TREINO" width="200" /> |

---

## ✨ Funcionalidades

### 1. Autenticação e Segurança
* **Login e Cadastro:** Integração via API com token **JWT (JSON Web Token)**.
* **Validações Client-Side:** Verificação de e-mail, campos vazios e confirmação de senha.
* **Persistência de Sessão:** Login automático utilizando `SharedPreferences` criptografado.

### 2. Gestão de Jogos (CRUD Completo)
* **Registro:** Cadastro detalhado com seletores inteligentes (**Spinners**) e calendário (**DatePicker**).
* **Dados:** Monitoramento de Gols, Assistências, Placar, Adversário e Minutagem.
* **Edição/Exclusão:** Menu de contexto (long click) para gerenciar registros errados.

### 3. Gestão de Treinos
* Controle de tipos de treino (Físico, Tático, Técnico, etc.).
* Registro de intensidade e nível de desgaste físico.

### 4. Perfil Híbrido Avançado
* **Integração com Câmera:** Captura de foto de perfil utilizando a Câmera nativa do dispositivo.
* **Persistência de Imagem:** A foto é salva no armazenamento interno e seu caminho (URI) persistido no banco local.
* **Cálculo Automático:** A idade é calculada dinamicamente com base na data de nascimento informada pelo usuário.
* **Isolamento de Dados:** Suporte a múltiplos usuários no mesmo dispositivo (cada login vê apenas seus dados locais).

---

## 🛠️ Stack Tecnológico

O projeto foi desenvolvido seguindo as melhores práticas do desenvolvimento Android Nativo moderno.

| Componente | Tecnologia | Descrição |
| :--- | :--- | :--- |
| **Linguagem** | **Kotlin** | Linguagem oficial, concisa e segura. |
| **Arquitetura** | **MVC / DAO** | Separação clara entre Interface, Lógica e Dados. |
| **Networking** | **Retrofit 2 + Gson** | Cliente HTTP type-safe para consumo da API. |
| **HTTP Client** | **OkHttp 4** | Interceptadores de Token e Logging de requisições. |
| **Banco Local** | **SQLite (Nativo)** | Implementação via `SQLiteOpenHelper` para persistência offline. |
| **Interface** | **XML Layouts** | Uso de LinearLayout, RelativeLayout e ScrollView. |
| **Design** | **Material Design** | CardViews, FloatingActionButtons e Cores Personalizadas. |

---

## 🔌 Consumo de API

A aplicação se comunica com um backend hospedado em `https://api-jogadores.onrender.com/`.

<details>
  <summary><strong>🔽 Ver Especificação dos Endpoints (JSON)</strong></summary>

### Autenticação
* `POST /auth/login`: Envia credenciais e recebe Token JWT + Dados do Usuário.
* `POST /auth/register`: Cria nova conta.

### Jogos
* `GET /jogos`: Retorna lista de partidas do usuário logado.
* `POST /jogos`: Cria nova partida.
    ```json
    {
      "data": "2025-11-23",
      "adversario": "Flamengo",
      "tipo": "Campeonato",
      "resultado": "Vitória",
      "gols": 2,
      "assistencias": 1,
      "nota": 8.5,
      "sensacao": 90
    }
    ```
* `PUT /jogos/{id}`: Atualiza partida existente.
* `DELETE /jogos/{id}`: Remove partida.

### Treinos
* `GET /treinos`: Retorna lista de treinos.
* `POST /treinos`: Cria novo treino.
    ```json
    {
      "data": "2025-11-24",
      "tipo": "Físico",
      "duracaoMin": 60,
      "intensidade": "Alta",
      "sensacao": 8
    }
    ```
</details>

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* Android Studio Iguana (ou superior).
* JDK 17 configurado.
* Dispositivo Android ou Emulador (API 24+).

### Passos
1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/douglasbarbosaoliveira/DiarioJogadorApp.git](https://github.com/douglasbarbosaoliveira/DiarioJogadorApp.git)
    ```
2.  **Abra no Android Studio:**
    Aguarde a indexação e o download das dependências do Gradle.
3.  **Configure a Internet:**
    Certifique-se de que o emulador tenha acesso à internet (necessário para a API).
4.  **Execute:**
    Clique no botão **Run** ▶️ e aproveite!

---

## 👥 Autores

Projeto acadêmico desenvolvido por:

* **Douglas Barbosa de Oliveira**
* **Lucas Casagrande Silva**
* **Luís Fernando França Farias**
* **Ryan Pereira da Mota**

