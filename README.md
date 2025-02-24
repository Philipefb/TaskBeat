# TaskBeats 📝

## Sobre o Projeto
TaskBeats é um aplicativo Android de gerenciamento de tarefas desenvolvido em Kotlin. O aplicativo permite aos usuários criar, organizar e gerenciar tarefas através de categorias personalizadas.

## 🛠️ Tecnologias Utilizadas
- Kotlin
- Android SDK
- Room Database (SQLite)
- RecyclerView
- Material Design Components
- BottomSheetDialogFragment
- ViewBinding

## ✨ Funcionalidades
- Criação e gerenciamento de categorias
- Adição, edição e remoção de tarefas
- Filtro de tarefas por categoria
- Interface intuitiva com Material Design
- Persistência local de dados
- Suporte a operações CRUD completas

## 📱 Screenshots
<p align="center">
  <img src=https://github.com/user-attachments/assets/3301b876-d620-4a81-bd5b-63a0c649a47f
width="30%" />
  <img src=https://github.com/user-attachments/assets/60f52832-b407-43a5-a66a-cbafb87cb25a width="30%" </
</p>


## 🏗️ Arquitetura
O projeto utiliza:
- Room Database para persistência de dados
- DAO (Data Access Object) para operações no banco
- Entidades para Tasks e Categories
- BottomSheets para criação/edição de itens
- RecyclerView com Adapters customizados

## 💾 Estrutura do Banco de Dados
### Entidades:
- **CategoryEntity**
  - name (PK): String
  - isSelected: Boolean

- **TaskEntity**
  - id (PK): Long
  - name: String
  - category (FK): String

## 🎨 Design
O aplicativo segue as diretrizes do Material Design, utilizando:
- BottomSheets para interações
- FAB (Floating Action Button) para ações principais
- Chips para filtros de categoria
- RecyclerView para listas
- Material TextInputLayout para campos de texto
