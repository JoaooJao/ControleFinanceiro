const formulario = document.querySelector("form");

const Inome = document.querySelector("#nome");
const Iemail = document.querySelector("#email");
const Isenha = document.querySelector("#senha");
const Itelefone = document.querySelector("#telefone");


function cadastrarUsuario() {

    fetch("http://localhost:8080/usuarios", {

        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },

        method: "POST",

        body: JSON.stringify({
            nome: Inome.value,
            email: Iemail.value,
            senha: Isenha.value,
            telefone: Itelefone.value
        })

    })

    .then(function(res) {

        console.log(res);

        if (res.ok) {
            alert("Usuário cadastrado com sucesso!");
            limparFormulario();
        }

    })

    .catch(function(error) {
        console.error("Erro ao cadastrar usuário:", error);
        alert("Erro ao cadastrar usuário.");

    });

}


function limparFormulario() {

    Inome.value = "";
    Iemail.value = "";
    Isenha.value = "";
    Itelefone.value = "";

}


formulario.addEventListener("submit", function(event) {

    event.preventDefault();

    cadastrarUsuario();

});

