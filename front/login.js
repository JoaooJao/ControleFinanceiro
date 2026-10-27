const formulario = document.querySelector("form");
const Iemail = document.querySelector("#email");
const Isenha = document.querySelector("#senha");

async function fazerLogin() {
    try {
        const res = await fetch("http://localhost:8080/usuarios/login", {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: Iemail.value,
                senha: Isenha.value
            })
        });

        if (res.status === 401) {
            alert("E-mail ou senha incorretos.");
            return;
        }

        if (!res.ok) {
            alert("Erro no servidor. Tente novamente.");
            return;
        }

        const dados = await res.json();
        localStorage.setItem("token", dados.token);
        localStorage.setItem("usuario", JSON.stringify(dados.usuario));
        window.location.href = "home.html";

    } catch (error) {
        console.error("Erro ao fazer login:", error);
        alert("Não foi possível conectar à API.");
    }
}

formulario.addEventListener("submit", function (event) {
    event.preventDefault();
    fazerLogin();
});